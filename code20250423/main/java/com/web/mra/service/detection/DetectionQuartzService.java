package com.web.mra.service.detection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionQuartzService {
    private final DetectionFileService fileService;
    private final DetectionService detectionService;
    private final DetectionMetadataService metadataService;
    
    private final Set<String> loggedDepartments = Collections.synchronizedSet(new HashSet<>());
    private boolean summaryLogged = false;
    private LocalDate lastResetDate = null;
    private boolean osdIncompletedLogged = false;

    private static final Map<String, List<Integer>> DEPARTMENT_DETECTIONS = new HashMap<>();
    static {
        DEPARTMENT_DETECTIONS.put("ADC", Collections.singletonList(101));
        DEPARTMENT_DETECTIONS.put("CLD", Arrays.asList(102, 103, 104));
        DEPARTMENT_DETECTIONS.put("OSD", Arrays.asList(105, 106));
    }
    
    private static final int SPECIAL_DETECTION_ID = 104;
    private static final LocalTime SPECIAL_DETECTION_TIME = LocalTime.of(13, 30);
    private static final LocalTime OSD_CHECK_TIME = LocalTime.of(12, 30);
    
    
    public void hourlyDetetionTask() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        boolean isLastRun = now.getHour() >= 15;
        
        if (lastResetDate == null || !lastResetDate.equals(today)) {
            loggedDepartments.clear();
            summaryLogged = false;
            osdIncompletedLogged = false;
            lastResetDate = today;
            log.info("Logging state reset for new day: {}", today);
        }
        
        // Step 1: Check if all detections are done today
        List<Integer> allDetectionIds = Arrays.asList(101, 102, 103, 104, 105, 106);
        boolean allDone = allDetectionIds.stream()
                .allMatch(id -> metadataService.doneTodayDetection(id));
        
        if (allDone) {
            log.info("All detections completed successfully for today: {}", today);
            if (!summaryLogged) {
                logSummary(today);
                summaryLogged = true;
            }
            checkSharedFolder(isLastRun);
            return ;
        }
        
        // Step 2: Process incomplete detections
        List<String> successfulDetections = new ArrayList<>();
        for (Integer detectionId : allDetectionIds) {
            if (!metadataService.doneTodayDetection(detectionId)) {
                if (detectionId == SPECIAL_DETECTION_ID) {
                    if (now.toLocalTime().isAfter(SPECIAL_DETECTION_TIME)) {
                        try {
                            String result = detectionService.detectNearMiss(detectionId, today, false);
                            if (metadataService.doneTodayDetection(detectionId)) {
                                successfulDetections.add(result);
                                log.info("Successfully ran detection for ID : {} (special case after 12:30 PM", detectionId);
                            }
                        } catch (Exception e) {
                            log.info("Failed to run detection ID : {} (special case)", detectionId, e);
                        }
                    } else {
                        log.info("Skipping detection ID : {} - waiting until aftger 12:30 PM", detectionId);
                    }
                } else {
                    String filePatterns = metadataService.getFilePatternList(detectionId);
                    if (fileService.filesAllReady(filePatterns)) {
                        try {
                            String result = detectionService.detectNearMiss(detectionId, today, false);
                            if (metadataService.doneTodayDetection(detectionId)) {
                                successfulDetections.add(result);
                                log.info("Successfully ran detection for ID : {}", detectionId);
                            }
                        } catch (Exception e) {
                            log.info("Failed to run detection ID : {}", detectionId, e);
                        }
                    } else {
                        log.info("Skipping detection ID : {} - required files not ready", detectionId);
                    }
                }
            }
        }
        
        // Now Check: Log if OSD detections are not all done after 12:30 PM
        if (now.toLocalTime().isAfter(OSD_CHECK_TIME) && !osdIncompletedLogged) {
            List<Integer> osdDetectionIds = DEPARTMENT_DETECTIONS.get("OSD");
            boolean allOsdDone = osdDetectionIds.stream()
                    .allMatch(id -> metadataService.doneTodayDetection(id));
            if (!allOsdDone) {
                List<Integer> notUpload = osdDetectionIds.stream().filter(id -> !metadataService.doneTodayDetection(id)).collect(Collectors.toList());
                detectionService.SendEmailUpload(notUpload);
                log.info("OSD department detections (105, 106) are not ready for all files uploading after 12:30 PM on {}", today);
                osdIncompletedLogged = true;
            }
        }
            
        // Step 3: Check department completion
        checkDepartmentCompletion(today, isLastRun);
        
        // Step 4: Check overall completion and handle last run
        checkOverallCompletion(today, isLastRun);
        
        // Step 5: Check if files existing in shared folder
        checkSharedFolder(isLastRun);
    }
    
    private void checkDepartmentCompletion(LocalDate today, boolean isLastRun) {
        // log.info("Enter checkDepartmentCompletion ");
        for (Map.Entry<String, List<Integer>> entry : DEPARTMENT_DETECTIONS.entrySet()) {
            String department = entry.getKey();
            List<Integer> detectionIds = entry.getValue();
            
            boolean allDeptDone = detectionIds.stream()
                    .allMatch(id -> metadataService.doneTodayDetection(id));
            
            log.info("Detection by  dept {}, status {} ", department, allDeptDone);
            if ((allDeptDone|| isLastRun) && !loggedDepartments.contains(department)) {
                detectionService.SendEmailNearMissEvents(department);
                log.info("All detection tasks completed for department: {}", department);
                loggedDepartments.add(department);
            }
        }
    }
    
    private void checkOverallCompletion(LocalDate today, boolean isLastRun) {
        // log.info("Enter checkOverallCompletion ");
        List<Integer> allDetectionIds = Arrays.asList(101, 102, 103, 104, 105, 106);
        boolean allDone = allDetectionIds.stream()
                    .allMatch(id -> metadataService.doneTodayDetection(id));
            
        if ((allDone || isLastRun) && (!summaryLogged)) {
            logSummary(today);
            summaryLogged = true;
        }
    }
    
    private void logSummary(LocalDate today) {
        // send email to ORD
        log.info("Generate summary for all detections {}", today);
        detectionService.SendEmailNearMissEvents("ORD");
        metadataService.upsertNearMissMonthlyHist();
    }
    
    private void checkSharedFolder(boolean isLastRun) {
        if (!isLastRun) {
            return;
        }
        String filesInSharedFolder = fileService.hasFileInSharedFolder();
        if (filesInSharedFolder != null && filesInSharedFolder.length() > 0) {
            // send email to ERM
            log.info("Send email to ERM Admin for file existing in shared folder ");
            detectionService.SendEmailNearMissEvents("ERM");
        }
    }
}
