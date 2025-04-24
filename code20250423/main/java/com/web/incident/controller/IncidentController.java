package com.web.incident.controller;

import com.boc.core.web.controller.BaseController;
import com.boc.core.web.domain.AjaxResult;
import com.boc.core.utils.poi.ExcelUtil;
import com.boc.utils.StringUtils;
import com.web.incident.entity.Incident;
import com.web.incident.entity.IncidentExport;
import com.web.incident.entity.request.IncidentExportRequest;
import com.web.incident.service.IncidentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"INCIDENT MANAGEMENT"})
@RestController
@RequestMapping("/mra/incident")
public class IncidentController extends BaseController {

    @Autowired
    private IncidentService incidentService;
    
    @ApiOperation(value = "export Incident")
    @PostMapping("/exportIncident")
    public void exportDataIncident(HttpServletResponse response, @RequestBody IncidentExportRequest request) {
        logger.info("IncidentController.exportIncident request:{}", request);
        try {
            Map<String, Object> map = new HashMap<>();
           
            if (StringUtils.isNotEmpty(request.getOwnerDept())) {
                map.put("ownerDept", request.getOwnerDept());
            }
            
            if (StringUtils.isNotEmpty(request.getPrimaryRiskType())) {
                map.put("primaryRiskType", request.getPrimaryRiskType());
            }
            
            
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (StringUtils.isNotEmpty(request.getIdentificationDateFrom())) {
                String fromDate = request.getIdentificationDateFrom();
                LocalDate startDate = LocalDate.parse(fromDate, fmt);
                map.put("identificationDateFrom", startDate.toString());
            }
            
            if (StringUtils.isNotEmpty(request.getIdentificationDateTo())) {
                String toDate = request.getIdentificationDateTo();
                LocalDate endDate = LocalDate.parse(toDate, fmt);
                map.put("identificationDateTo", endDate.toString());
            }


            List<IncidentExport> incidentExportList = incidentService.exportIncident(map);
            
            logger.info("IncidentController.exportIncident size {}", incidentExportList.size());
            ExcelUtil<IncidentExport> incidentExcelUtil = new ExcelUtil<>(IncidentExport.class);
            incidentExcelUtil.exportExcel(response, incidentExportList, "Incident", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "Query Incident List")
    @GetMapping("/list")
    public AjaxResult getIncidentList(
            @RequestParam(required = false) String incidentNo,
            @RequestParam(required = false) String incidentStatus,
            @RequestParam(required = false) String ownerDept,
            @RequestParam(required = false) String primaryRiskType,
            @RequestParam(required = false) String identificationDateFrom,
            @RequestParam(required = false) String identificationDateTo
    ) {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(incidentNo)) {
            params.put("incidentNo", incidentNo);
        }
        if (StringUtils.isNotEmpty(incidentStatus)) {
            params.put("incidentStatus", incidentStatus);
        }
        if (StringUtils.isNotEmpty(ownerDept)) {
            params.put("ownerDept", ownerDept);
        }
        if (StringUtils.isNotEmpty(primaryRiskType)) {
            params.put("primaryRiskType", primaryRiskType);
        }

        if (StringUtils.isNotEmpty(identificationDateFrom)) {
            params.put("identificationDateFrom", identificationDateFrom);
        }
        if (StringUtils.isNotEmpty(identificationDateTo)) {
            params.put("identificationDateTo", identificationDateTo);
        }

        List<Incident> incidents = incidentService.selectIncidentList(params);
        return AjaxResult.success(incidents);
    }
}
