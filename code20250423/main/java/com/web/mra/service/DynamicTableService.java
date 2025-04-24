package com.web.mra.service;

import com.web.mra.dto.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicTableService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    
    private static final Set<String> WRITEABLE_ALLOWED_TABLES = new HashSet<>(Arrays.asList("IMP_DETECTION_DICT", "IMP_DETECTION_EXCEL_META"));
    
    private static final Set<String> READABLE_ONLY_ALLOWED_TABLES = new HashSet<>(Arrays.asList("IMP_DETECTION", "IMP_DETECTION_AUDIT_TRAIL"));


    public boolean isWriteableTable(String tableAlias) {
        if (tableAlias == null || tableAlias.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        return WRITEABLE_ALLOWED_TABLES.contains(tableAlias);
    }
    
    public boolean isReadableTable(String tableAlias) {
        if (tableAlias == null || tableAlias.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        return WRITEABLE_ALLOWED_TABLES.contains(tableAlias) || READABLE_ONLY_ALLOWED_TABLES.contains(tableAlias);
    }
    
    
    public PageResponse<Map<String, Object>> getTableData(
            String tableAlias, int page, int size
    ) {
        if (!isReadableTable(tableAlias)) {
            throw new IllegalArgumentException("Table " + tableAlias + " is not readable");
        }
        
        String sql = "";
        if ("IMP_DETECTION".equals(tableAlias.toUpperCase())) {
           sql = "SELECT *, FORMAT(create_time, 'yyyy-MM-ddTHH:mm:ss') AS create_time_ets  FROM web.IMP_DETECTION ORDER BY auto_id DESC " ; 
        } else if ("IMP_DETECTION_EXCEL_META".equals(tableAlias.toUpperCase())) {
            sql = "SELECT * FROM web.IMP_DETECTION_EXCEL_META ORDER BY auto_id ASC " ;      
        } else if ("IMP_DETECTION_DICT".equals(tableAlias.toUpperCase())) {
             sql = "SELECT * FROM web.IMP_DETECTION_DICT ORDER BY auto_id ASC " ;      
        } else if ("IMP_DETECTION_AUDIT_TRAIL".equals(tableAlias.toUpperCase())) {
             sql = "SELECT * FROM web.IMP_DETECTION_AUDIT_TRAIL ORDER BY auto_id DESC " ;      
        } else {
            return null;
        }
       
        // Execute query
        Map<String, Object> params = new HashMap<>();

        List<Map<String, Object>> content = jdbcTemplate.queryForList(
                sql, params
        );

        // Get total count
        long total = content.size();

        // Create response
        PageResponse<Map<String, Object>> response = new PageResponse<>();
        response.setContent(content);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);

        return response;
    }

   

    public Map<String, Object> updateRecord(
            String tableAlias, Long id, Map<String, Object> record
    ) {
        if (!isWriteableTable(tableAlias)) {
            throw new IllegalArgumentException("Table " + tableAlias + " is not writeable");
        }
        // Remove null values and ensure id matches
        Map<String, Object> oldRecord = getRecordById(tableAlias, id);
        record.values().removeIf(Objects::isNull);

        String sql = "";
        Map<String, Object> params = new HashMap<>();
        params.put("auto_id", id);
        
        if ("IMP_DETECTION_DICT".equals(tableAlias.toUpperCase()) && record.containsKey("string_value")) {
            params.put("string_value", record.get("string_value"));
            sql = "UPDATE web.IMP_DETECTION_DICT SET string_value = :string_value WHERE auto_id = :auto_id";
        } else if ("IMP_DETECTION_EXCEL_META".equals(tableAlias.toUpperCase()) && record.containsKey("cell_params")) {
            params.put("cell_params", record.get("cell_params"));
             sql = "UPDATE web.IMP_DETECTION_EXCEL_META SET cell_params = :cell_params WHERE auto_id = :auto_id" ;      
        } else {
            return null;
        }
       

        jdbcTemplate.update(sql, new MapSqlParameterSource(params));
        
        Map<String, Object> updatedRecord = getRecordById(tableAlias, id);
        
        record.forEach((field, newValue) -> {
            if ("string_value".equals(field) || "cell_params".equals(field)) {
                Object oldValue = oldRecord.get(field);
                if (!Objects.equals(oldValue, newValue)) {
                    auditService.logAudit(null, "UPDATE", "Updated record in " + tableAlias + " with ID: " + id,
                            tableAlias, field, 
                            oldValue != null ? oldValue.toString() : null, 
                            newValue != null ? newValue.toString() : null
                    );
                }
            }
        });
        return updatedRecord;
    }

    private Map<String, Object> getRecordById(String tableAlias, Long id) {
        if (!isReadableTable(tableAlias)) {
            throw new IllegalArgumentException("Table " + tableAlias + " is not readable");
        }
        String sql = "";
        if ("IMP_DETECTION_DICT".equals(tableAlias.toUpperCase())) {
            sql = "SELECT * FROM web.IMP_DETECTION_DICT WHERE auto_id = :id";
        } else if ("IMP_DETECTION_EXCEL_META".equals(tableAlias.toUpperCase())) {
              sql = "SELECT * FROM web.IMP_DETECTION_EXCEL_META WHERE auto_id = :id" ;      
        } else if ("IMP_DETECTION".equals(tableAlias.toUpperCase())) {
            sql = "SELECT * FROM web.IMP_DETECTION WHERE auto_id = :id";
        } else {
            return null;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return jdbcTemplate.queryForMap(sql, params);
    }
}

