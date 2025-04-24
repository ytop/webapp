package com.web.incident.service.serviceImpl;

import com.boc.core.utils.StringUtils;
import com.web.incident.dao.IncidentMapper;
import com.web.incident.entity.Incident;
import com.web.incident.entity.IncidentExport;
import com.web.incident.service.IncidentService;
import com.web.risk.entity.Inventory;
import com.web.risk.entity.KriInventoryExport;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

@Service
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentMapper incidentMapper;



    @Override
    public List<Incident> selectIncidentList(Map<String, Object> map) {
        return incidentMapper.selectIncidentList(map);
    }

    @Override
    public List<Incident> selectIncidentPage(Map<String, Object> map) {
        return incidentMapper.selectIncidentPage(map);
    }
    
    @Override
    public List<IncidentExport> exportIncident(Map<String,Object> map) {
        List<Incident> incidentList = incidentMapper.selectIncidentList(map);
        List<IncidentExport> incidentExports = new ArrayList<>();
        if(StringUtils.isNotEmpty(incidentList)){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            incidentList.forEach(item -> {
                IncidentExport export = new IncidentExport();
                BeanUtils.copyProperties(item, export, "occurrenceDate", "identificationDate");
                if (item.getOccurrenceDate() != null) {
                    export.setOccurrenceDate(formatter.format(item.getOccurrenceDate()));
                } else {
                    export.setOccurrenceDate("");
                }
                if (item.getIdentificationDate() != null) {
                    export.setIdentificationDate(formatter.format(item.getIdentificationDate()));
                } else {
                    export.setIdentificationDate("");
                }
                incidentExports.add(export);
            });
        }
        return incidentExports;
    }
}
