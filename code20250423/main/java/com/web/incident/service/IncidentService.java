package com.web.incident.service;

import com.web.incident.entity.Incident;
import com.web.incident.entity.IncidentExport;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IncidentService {
    /**
     * Query incident list with filters
     */
    List<Incident> selectIncidentList(Map<String, Object> map);

    /**
     * Get paginated incidents
     */
    List<Incident> selectIncidentPage(Map<String, Object> map);
    
    /** Download IMP_INCIDENT partial data */
    List<IncidentExport> exportIncident(Map<String,Object> map);
}
