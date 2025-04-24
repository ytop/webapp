package com.web.mra.controller;

import com.boc.core.web.domain.AjaxResult;
import com.web.mra.service.AuditService;
import com.web.mra.service.detection.DetectionService;
import com.web.mra.service.detection.DetectionFileService;
import com.web.mra.dto.FileDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Api(tags = {"DETECTION MANAGEMENT"})
@RestController
@RequestMapping("/mra/admin")
@RequiredArgsConstructor
@Slf4j
public class DetectionController {

    private final DetectionService detectionService;
    private final DetectionFileService detectionFileService;
    private final AuditService auditService;

    @ApiOperation(value = "Execute Near Miss Detection")
    @GetMapping("/detection/list/{detectionId}")
    public AjaxResult executeNearMissDetection(@PathVariable Integer detectionId) {
        try {
            String result = detectionService.detectNearMiss(detectionId, LocalDate.now(), true);
            
            auditService.logAudit(null, "FORCE_DETECT_NEAR_MISS", "Executed near miss detection for ID: " + detectionId,
                    null, null, null, null);
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("Error executing near miss detection", e);
            return AjaxResult.error("Detection failed: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get Last 8 days Miss Detection Results")
    @GetMapping("/detection/selectWeekly")
    public AjaxResult getWeeklyNearMissDetections() {
        try {
            Map<String, Object> results = detectionService.getWeeklyNearMiss();
            return AjaxResult.success(results);
        } catch (Exception e) {
            log.error("Error getting all near miss detection results", e);
            return AjaxResult.error("Failed to get detection results: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get history of near miss event")
    @GetMapping("/detection/selectNearMissHistory")
    public AjaxResult getNearMissHistory() {
        try {
            Map<String, Object> results = detectionService.getNearMissHistory();
            return AjaxResult.success(results);
        } catch (Exception e) {
            log.error("Error get  history of near miss event", e);
            return AjaxResult.error("Failed to get  history of near miss event: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Upload File")
    @PostMapping("/upload")
    public  AjaxResult uploadFiles(@RequestParam("file") MultipartFile[] files) {
        try {
            List<String> fileList = detectionFileService.uploadFiles(files);
            List<String> results = detectionService.fileTriggerDetection(fileList);
            
            String fileNames = String.join(", ", fileList);
            auditService.logAudit(null, "FILE_UPLOAD", "Uploaded files: " + fileNames,
                    null, null, null, null);
            return AjaxResult.success(results);
        } catch (Exception e) {
            return AjaxResult.error("Failed to upload files: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get File list")
    @GetMapping("/files")
    public  AjaxResult listFiles(@RequestParam(defaultValue = "") String path) {
        try {
            List<FileDTO> results = detectionFileService.listFiles(path);
            return AjaxResult.success(results);
        } catch (Exception e) {
            return AjaxResult.error("Failed to list files: " + e.getMessage());
        }
    }
}
