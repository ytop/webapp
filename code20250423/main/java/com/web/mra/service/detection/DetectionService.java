package com.web.mra.service.detection;

import com.boc.core.utils.poi.ExcelUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.incident.entity.IncidentExport;
import com.web.incident.entity.request.IncidentExportRequest;
import com.web.incident.service.IncidentService;
import com.web.mra.dto.*;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import static com.web.mra.service.detection.DetectionUtils.cleanJsonString;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionService {
    private final DetectionDataMartService dataMartService;
    private final DetectionExcelService excelService;
    private final DetectionMatchService matchService;
    private final DetectionMetadataService metadataService;
    private final DetectionEmailService detectionEmailService;
    private final DetectionFileService fileService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final ObjectMapper objectMapper;
    
    @Autowired
    private IncidentService incidentService;

    @Transactional
    public String detectNearMiss(Integer detectionId, LocalDate eventDate, boolean forceDetection) {
        try {
            validateDetectionId(detectionId);
            LocalDate effectiveDate = eventDate != null ? eventDate : LocalDate.now();
            
            // Skip if already done today's detection
            if (!forceDetection && metadataService.doneTodayDetection(detectionId)) {
                log.info(" SKIP Detection {}, already done today", detectionId);
                Map<String, Object> detectionSummmary = metadataService.getDetectionSummary(detectionId, effectiveDate);
                return objectMapper.writeValueAsString(detectionSummmary);
            }

            DetectionMetaDTO detectionMeta = metadataService.getDetectionMetaConfig(detectionId);
            if (detectionMeta == null) {
                throw new DetectionServiceException("No detection metadata found for ID: " + detectionId);
            }

            List<DetectionComponentMetaDTO> componentMetas = metadataService.getActiveComponentDetections(detectionId);
            List<CompletableFuture<DetectionResult>> futures = executeComponentDetections(componentMetas, forceDetection);
            processDetectionResults(detectionId, effectiveDate, detectionMeta, futures);

            Map<String, Object> detectionSummary = metadataService.getDetectionSummary(detectionId, effectiveDate);
            return objectMapper.writeValueAsString(detectionSummary);
        } catch (Exception e) {
            String errorMsg = String.format("Near miss detection failed for ID: %d and date: %s", detectionId,
                    eventDate);
            log.error(errorMsg, e);
            throw new DetectionServiceException(errorMsg, e);
        }
    }
    
    public List<String> fileTriggerDetection(List<String> fileList) {
        List<Integer> detectionIds = metadataService.getAllDetectionID();
        List<String> results = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Integer detectionId : detectionIds) {
            try {
                String filePatterns = metadataService.getFilePatternList(detectionId);
                
                if (!filePatterns.isEmpty() && 
                    fileService.fileInPattern(fileList, filePatterns) &&
                    fileService.filesAllReady(filePatterns)) {
                    
                    String result = detectNearMiss(detectionId, today, false);
                    results.add(result);
                }
            } catch (Exception e) {
                log.error("Failed to process file trigger detection for ID: " + detectionId, e);
            }
        }
        
        return results;
    }

    public List<String> detectAllNearMiss() {
        List<Integer> detectionIds = metadataService.getAllDetectionID();
        List<String> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Integer detectionId : detectionIds) {
            try {
                String result = detectNearMiss(detectionId, today, false);
                results.add(result);
            } catch (Exception e) {
                log.error("Failed to detect near miss for ID: " + detectionId, e);
            }
        }
        return results;
    }

    public Map<String, Object> getNearMissHistory() {
        return metadataService.getNearMissHist();
    }
    
    public Map<String, Object> getWeeklyNearMiss() {
        List<Map<String, Object>> weeklyEvents = metadataService.getRecentDetectionSummary(-7);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("detections", weeklyEvents);
        return resultMap;
    }
    
    public Boolean SendEmailUpload(List<Integer> ids) {
        log.info(" upload file reminder ");
        List<DetectionEmailMetaDTO> emailConfigs = metadataService.getAllDetectionEmailConfigs();
        try {
            Map<String, String> status = new HashMap<>();
            for (Integer id : ids) {
                Map<String, Object> summary = metadataService.getCurrentDaySummary(id);
                status.put(String.valueOf(summary.get("detection_desc")), String.valueOf(summary.get("detection_status")));
            }
            String nonUserEmails = metadataService.getNonUserEmail("OSD");
            detectionEmailService.SendEmailNearMissEventUploadFiles(emailConfigs, status, nonUserEmails);
        } catch (Exception e) {
            log.error("Failed to send uploading file reminder emails", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    public Boolean SendEmailNearMissEvents(String department) {
        log.info(" near miss events ");
        List<DetectionMetaDTO> allDetections = metadataService.getAllDetectionMetas();
        List<DetectionEmailMetaDTO> emailConfigs = metadataService.getAllDetectionEmailConfigs();

        try {
            Map<String, List<DetectionMetaDTO>> detectionsByDept = allDetections.stream().collect(Collectors.groupingBy(DetectionMetaDTO::getDetectionDept));

            if (department.equals("ERM")) {
                // ERM
                String ermEmail = metadataService.getERMEmail();
                String fileList = fileService.hasFileInSharedFolder();
                detectionEmailService.SendEmailNearMissEventsDetectedERM(fileList, ermEmail);
            } else if (department.equals("ORD") == false) {
                // OSD, CLD, ADC
                Map<String, String> nearMissCount = new HashMap<>();
                Map<String, String> nearMissStatus = new HashMap<>();
                Map<String, String> nearMissRunTime = new HashMap<>();
                int totalCount = 0;
                for (Map.Entry<String, List<DetectionMetaDTO>> entry : detectionsByDept.entrySet()) {
                    // only calculate related department
                    if (entry.getKey().equals(department) == false) {
                        continue;
                    }
                    List<DetectionMetaDTO> deptDetections = entry.getValue();
                    for (DetectionMetaDTO detection : deptDetections) {
                        Map<String, Object> summary = metadataService.getCurrentDaySummary(detection.getDetectionId());
                        String near_miss_status_num = String.valueOf(summary.get("near_miss_status"));
                        String near_miss_name = String.valueOf(summary.get("detection_desc"));
                        String near_miss_status = String.valueOf(summary.get("detection_status"));
                        String near_miss_runtime = "";
                        if (String.valueOf(summary.get("create_time")).length() < 10) {
                            near_miss_runtime = "";
                        } else {
                            near_miss_runtime = String.valueOf(summary.get("create_time")).substring(0, 10);
                            if (String.valueOf(summary.get("create_time")).length() > 10) near_miss_runtime += " " + String.valueOf(summary.get("create_time")).substring(11, 16);
                        }
                        nearMissCount.put(near_miss_name, near_miss_status_num);
                        nearMissStatus.put(near_miss_name, near_miss_status);
                        nearMissRunTime.put(near_miss_name, near_miss_runtime);
                        if (near_miss_status_num.equals("-") != true) totalCount += Integer.parseInt(near_miss_status_num);
                    }
                }
                String nonUserEmails = metadataService.getNonUserEmail(department);
                detectionEmailService.SendEmailNearMissEventsDetectedNonORD(department, emailConfigs, nonUserEmails, totalCount, nearMissCount, nearMissStatus, nearMissRunTime);
            } else {
                // ORD
                Map<String, String> nearMissCount = new HashMap<>();
                Map<String, String> nearMissStatus = new HashMap<>();
                Map<String, String> nearMissRunTime = new HashMap<>();
                Map<String, String> nearMissDept = new HashMap<>();
                int totalCount = 0;
                
                for (Map.Entry<String, List<DetectionMetaDTO>> entry : detectionsByDept.entrySet()) {
                    List<DetectionMetaDTO> deptDetections = entry.getValue();
                    for (DetectionMetaDTO detection : deptDetections) {
                        Map<String, Object> summary = metadataService.getCurrentDaySummary(detection.getDetectionId());
                        
                        String near_miss_status_num = String.valueOf(summary.get("near_miss_status"));
                        String near_miss_name = String.valueOf(summary.get("detection_desc"));
                        String near_miss_status = String.valueOf(summary.get("detection_status"));
                        String near_miss_runtime = "";
                        if (String.valueOf(summary.get("create_time")).length() < 10) {
                            near_miss_runtime = "";
                        } else {
                            near_miss_runtime = String.valueOf(summary.get("create_time")).substring(0, 10);
                            if (String.valueOf(summary.get("create_time")).length() > 10) near_miss_runtime += " " + String.valueOf(summary.get("create_time")).substring(11, 16);

                        }
                        String near_miss_dept = String.valueOf(summary.get("detection_dept"));
                        nearMissCount.put(near_miss_name, near_miss_status_num);
                        nearMissStatus.put(near_miss_name, near_miss_status);
                        nearMissRunTime.put(near_miss_name, near_miss_runtime);
                        nearMissDept.put(near_miss_name, near_miss_dept);
                        if (near_miss_status_num.equals("-") != true) totalCount += Integer.parseInt(near_miss_status_num);
                    }
                }
                
                String ormEmail = metadataService.getORMEmail();
                
                String ccEmail = metadataService.getORDccEmail();
                detectionEmailService.SendEmailNearMissEventsDetectedORD(totalCount, nearMissCount, nearMissStatus, nearMissRunTime, nearMissDept, 
                        exportTodayIncident(), ormEmail, ccEmail);
            }
        } catch (Exception e) {
            log.error("Failed to send detection summary emails", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public String exportTodayIncident() {
        try {
            Map<String, Object> map = new HashMap<>();
            LocalDate today = LocalDate.now();
            map.put("identificationDateFrom", today);
            map.put("identificationDateTo", today);

            List<IncidentExport> incidentExportList = incidentService.exportIncident(map);
            
            if (incidentExportList.size() == 0) {
                return "";
            }
            
            ExcelUtil<IncidentExport> incidentExcelUtil = new ExcelUtil<>(IncidentExport.class);
            // return fileService.saveWBtoFile(incidentExcelUtil.buildExcel(incidentExportList, "Sheet1", "Near Miss Events"));
            return fileService.saveWBtoFile(incidentExcelUtil.buildExcel(incidentExportList, "Sheet1", null));
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private List<CompletableFuture<DetectionResult>> executeComponentDetections(
            List<DetectionComponentMetaDTO> componentMetas, boolean forceDetection) {
        List<CompletableFuture<DetectionResult>> futures = new ArrayList<>();

        for (DetectionComponentMetaDTO componentMeta : componentMetas) {
            CompletableFuture<DetectionResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Object result = executedetectionComponent(componentMeta.getDetectionComponentId(), forceDetection);
                    return new DetectionResult(componentMeta.getDetectionComponentId(), result, null);
                } catch (Exception e) {
                    log.error("Component detection failed", e);
                    return new DetectionResult(componentMeta.getDetectionComponentId(), null, e);
                }
            }, executorService);
            futures.add(future);
        }

        return futures;
    }

    private void processDetectionResults(Integer detectionId, LocalDate effectiveDate, DetectionMetaDTO detectionMeta,
            List<CompletableFuture<DetectionResult>> futures) {
        boolean allSuccess = true;
        StringBuilder detectionDetail = new StringBuilder();
        List<Integer> failedComponentIds = new ArrayList<>();

        for (CompletableFuture<DetectionResult> future : futures) {
            try {
                DetectionResult result = future.get(5, TimeUnit.MINUTES);
                Integer componentId = result.getDetectionComponentId();
                Object resultValue = result.getResult();
                boolean isEmpty = resultValue == null || resultValue.toString().trim().isEmpty()
                        || "{}".equals(resultValue.toString().trim()) || result.getError() != null;

                if (isEmpty) {
                    allSuccess = false;
                    failedComponentIds.add(componentId);
                    if (result.getError() != null) {
                        detectionDetail.append(" - ").append(result.getError().getMessage());
                    }
                    metadataService.notifyFailure(detectionId, componentId,
                            result.getError() != null ? result.getError().getMessage() : "Empty detection");
                } else {
                    log.info(" detect successful");
                }
            } catch (Exception e) {
                allSuccess = false;
                handleDetectionError(detectionDetail, e);
            }
        }

        if (allSuccess) {
            String matchResult = matchService.executeMatchByDate(detectionId, effectiveDate);
            detectionDetail.append(matchResult);
            metadataService.saveDetectionSummary(detectionId, detectionMeta, effectiveDate,
                    cleanJsonString(detectionDetail.toString()), "Successful");
        } else {
            log.info("Detection failed due to component issues: {}", failedComponentIds);
            metadataService.saveDetectionSummary(detectionId, detectionMeta, effectiveDate,
                    cleanJsonString(detectionDetail.toString()), "Failed");
        }
    }

    private Object executedetectionComponent(Integer detectionComponentId, boolean forceDetection) {
        if (detectionComponentId < 0) {
            return null;
        }
        
        // Check if today's detection result already exists
        Integer countDetection =  metadataService.getCountDetectionResult(detectionComponentId, LocalDate.now());
        
        if (!forceDetection && countDetection > 0) {
            String existingResult = metadataService.getDetectionResult(detectionComponentId, LocalDate.now());
            if (existingResult != null && !existingResult.isEmpty()) {
                log.info("Detection result already exists for component ID {} on date: {}", detectionComponentId, LocalDate.now());
                return existingResult;
            }
        }

        DetectionComponentMetaDTO componentMeta = metadataService.getDetectionCompMetaConfig(detectionComponentId);
        validateComponentMeta(componentMeta, detectionComponentId);

        try {
            Object detectionResult = executeDetectionBySource(detectionComponentId, componentMeta);
            String resultString = cleanJsonString((String) detectionResult);
            metadataService.saveDetectionResult(detectionComponentId, resultString);
            return resultString;
        } catch (Exception e) {
            log.error("Error executing detection for component ID: {}", detectionComponentId, e);
            throw new DetectionServiceException("Detection execution failed", e);
        }
    }

    private Object executeDetectionBySource(Integer detectionComponentId, DetectionComponentMetaDTO componentMeta) {
        Integer source = componentMeta.getDetectionComponentSource();

        switch (source) {
        case DetectionSource.DATA_MART:
            return dataMartService.detectDataMart(metadataService.getDetectionDataMartMetaConfig(detectionComponentId));
        case DetectionSource.EXCEL:
            return excelService.detectExcel(metadataService.getDetectionExcelMetaConfig(detectionComponentId));
        default:
            throw new IllegalArgumentException("Unsupported detection source type: " + source);
        }
    }

    private void validateDetectionId(Integer detectionId) {
        if (detectionId == null || detectionId <= 0) {
            throw new IllegalArgumentException("Invalid Detection ID: " + detectionId);
        }
    }

    private void validateComponentMeta(DetectionComponentMetaDTO componentMeta, Integer detectionComponentId) {
        if (componentMeta == null) {
            throw new IllegalArgumentException("No metadata found for detection ID: " + detectionComponentId);
        }
        if (!"Y".equals(componentMeta.getIsActive())) {
            log.warn("Detection component {} is inactive", detectionComponentId);
        }
    }

    private void handleDetectionError(StringBuilder detectionDetail, Exception e) {
        if (e instanceof TimeoutException) {
            detectionDetail.append("Component execution timed out\n");
            log.error("Detection timeout", e);
        } else {
            detectionDetail.append("Component execution failed: ").append(e.getMessage()).append("\n");
            log.error("Detection execution error", e);
        }
    }

    @Override
    protected void finalize() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
