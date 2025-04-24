package com.web.mra.service.detection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import com.web.mra.dto.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.web.mra.service.detection.DetectionUtils.cleanJsonString;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionMetadataService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "detectionMetaConfig", key = "#detectionId")
    public DetectionMetaDTO getDetectionMetaConfig(Integer detectionId) {
        try {
            String sql = "SELECT * FROM web.IMP_DETECTION_META WHERE detection_id = :detectionId";
            Map<String, Object> params = Collections.singletonMap("detectionId", detectionId);

            return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                    DetectionMetaDTO.builder()
                            .autoId(rs.getLong("auto_id"))
                            .detectionId(rs.getInt("detection_id"))
                            .detectionDept(rs.getString("detection_dept"))
                            .detectionDesc(rs.getString("detection_desc"))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching detection meta configuration for ID: {}", detectionId, e);
            return null;
        }
    }

    @Cacheable(value = "allDetectionMetas")
    public List<DetectionMetaDTO> getAllDetectionMetas() {
        try {
            String sql = "SELECT * FROM web.IMP_DETECTION_META";

            return jdbcTemplate.query(sql, (rs, rowNum) ->
                    DetectionMetaDTO.builder()
                            .autoId(rs.getLong("auto_id"))
                            .detectionId(rs.getInt("detection_id"))
                            .detectionDept(rs.getString("detection_dept"))
                            .detectionDesc(rs.getString("detection_desc"))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching all detection meta configurations", e);
            return Collections.emptyList();
        }
    }

    public List<DetectionComponentMetaDTO> getActiveComponentDetections(Integer detectionId) {
        String sql = "SELECT * FROM web.IMP_DETECTION_COMPONENT_META " +
                "WHERE detection_component_id / 100 = :detectionId AND is_active = 'Y'";

        Map<String, Object> params = Collections.singletonMap("detectionId", detectionId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                DetectionComponentMetaDTO.builder()
                        .autoId(rs.getLong("auto_id"))
                        .detectionComponentId(rs.getInt("detection_component_id"))
                        .componentLabels(rs.getString("component_labels"))
                        .componentDataTypes(rs.getInt("component_data_types"))
                        .detectionComponentSource(rs.getInt("component_detection_source"))
                        .detectionComponentDescription(rs.getString("component_detection_description"))
                        .isActive(rs.getString("is_active"))
                        .build()
        );
    }

    public DetectionDataMartMetaDTO getDetectionDataMartMetaConfig(Integer detectionComponentId) {
        String sql = " SELECT * FROM web.IMP_DETECTION_DATA_MART_META    WHERE detection_component_id = :detectionComponentId";
        Map<String, Object> params = Collections.singletonMap("detectionComponentId", detectionComponentId);

        try {
            return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                    DetectionDataMartMetaDTO.builder()
                            .autoId(rs.getLong("auto_id"))
                            .detectionComponentId(rs.getInt("detection_component_id"))
                            .sqlTemplate(rs.getString("sql_template"))
                            .sqlParams(rs.getString("sql_params"))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching detection data mart meta configuration for ID: {}",
                    detectionComponentId, e);
            throw new DetectionServiceException("Failed to get Data Mart configuration", e);
        }
    }

    public DetectionExcelMetaDTO getDetectionExcelMetaConfig(Integer detectionComponentId) {
        String sql = "   SELECT * FROM web.IMP_DETECTION_EXCEL_META     WHERE detection_component_id = :detectionComponentId ";
        Map<String, Object> params = Collections.singletonMap("detectionComponentId", detectionComponentId);

        try {
            return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                    DetectionExcelMetaDTO.builder()
                            .autoId(rs.getLong("auto_id"))
                            .detectionComponentId(rs.getInt("detection_component_id"))
                            .excelFileName(rs.getString("excel_file_name"))
                            .sheetName(rs.getString("sheet_name"))
                            .cellPositions(rs.getString("cell_positions"))
                            .cellParams(rs.getString("cell_params"))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching detection excel meta configuration for ID: {}",
                    detectionComponentId, e);
            throw new DetectionServiceException("Failed to get Excel configuration", e);
        }
    }

    public List<DetectionEmailMetaDTO> getAllDetectionEmailConfigs() {
        String sql = "SELECT * FROM web.IMP_DETECTION_EMAIL_META WHERE is_active = 'Y'";

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) ->
                    DetectionEmailMetaDTO.builder()
                            .autoId(rs.getLong("auto_id"))
                            .userName(rs.getString("user_name"))
                            .userDepartment(rs.getString("user_department"))
                            .emailGroupBitmap(rs.getInt("email_group_bitmap"))
                            .isActive(rs.getString("is_active"))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching all detection email configurations", e);
            throw new DetectionServiceException("Failed to get Email configurations", e);
        }
    }
    
    public String getDetectionDescriptionTemplate(Integer detectionId) {
        try {
            String sql = "SELECT string_value FROM web.IMP_DETECTION_DICT " +
                    "WHERE dict_id = 2 AND int_value = :detectionId " ;
            Map<String, Object> params = new HashMap<>();
            params.put("detectionId", detectionId);
            return jdbcTemplate.queryForObject(sql, params, String.class);
        } catch (Exception e) {
            log.error("Error retrieving detection description for ID: {}", detectionId, e);
            return "";
        }
    }

    
    private String getEmailAddressFromDict(Integer deptEmailID) {
        try {
            String sql = "SELECT string_value FROM web.IMP_DETECTION_DICT " +
                    "WHERE dict_id = 3 AND int_value = :deptEmailID " ;
            Map<String, Object> params = new HashMap<>();
            params.put("deptEmailID", deptEmailID);
            return jdbcTemplate.queryForObject(sql, params, String.class);
        } catch (Exception e) {
            log.error("Error retrieving ORM Email", e);
            return "";
        }
    }
    
    public String getORMEmail() {
        return getEmailAddressFromDict(1);
    }
    
    public String getORDccEmail() {
        return getEmailAddressFromDict(2);
    }
    
    public String getERMEmail() {
        return getEmailAddressFromDict(3);
    }
    
    public String getNonUserEmail(String department) {
        if ("ADC".equals(department)) {
            return getEmailAddressFromDict(4);
        }
        if ("CLD".equals(department)) {
            return getEmailAddressFromDict(5);
        }
        if ("OSD".equals(department)) {
            return getEmailAddressFromDict(6);
        }
        return "";
    }
    
    public Integer getCountDetectionResult(Integer detectionComponentId, LocalDate eventDate) {
        if (detectionComponentId <= 0) {
            return 0;
        }
        try {
                String sql = "SELECT COUNT(component_detection_result) " +
                        "FROM web.IMP_DETECTION_COMPONENT " +
                        "WHERE detection_component_id = :detectionComponentId " +
                        "AND event_date = :eventDate " +
                        "AND create_time = (SELECT MAX(create_time) " +
                        "                  FROM web.IMP_DETECTION_COMPONENT " +
                        "                  WHERE detection_component_id = :detectionComponentId " +
                        "                  AND event_date = :eventDate " +
                        "                  AND (LEN(component_detection_result) > 2 OR detection_component_id <> 10401))";

                Map<String, Object> params = new HashMap<>();
                params.put("detectionComponentId", detectionComponentId);
                params.put("eventDate", eventDate);

                return jdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            log.error("Error retrieving number of detection result for ID: {}", detectionComponentId, e);
            return 0;
        }
          
    }
    
    public String getDetectionResult(Integer detectionComponentId, LocalDate eventDate) {
        if (detectionComponentId == 0) {
            return "";
        }
        try {
            if (detectionComponentId > 0) {
                String sql = "SELECT component_detection_result " +
                        "FROM web.IMP_DETECTION_COMPONENT " +
                        "WHERE detection_component_id = :detectionComponentId " +
                        "AND event_date = :eventDate " +
                        "AND create_time = (SELECT MAX(create_time) " +
                        "                  FROM web.IMP_DETECTION_COMPONENT " +
                        "                  WHERE detection_component_id = :detectionComponentId " +
                        "                  AND event_date = :eventDate)";

                Map<String, Object> params = new HashMap<>();
                params.put("detectionComponentId", detectionComponentId);
                params.put("eventDate", eventDate);

                return jdbcTemplate.queryForObject(sql, params, String.class);
            } else {
                String sql = "SELECT component_detection_result " +
                        "FROM web.IMP_DETECTION_COMPONENT_CONST_META " +
                        "WHERE detection_component_id = :detectionComponentId";

                Map<String, Object> params = new HashMap<>();
                params.put("detectionComponentId", detectionComponentId);

                return jdbcTemplate.queryForObject(sql, params, String.class);
            }
        } catch (Exception e) {
            log.error("Error retrieving detection result for ID: {}", detectionComponentId, e);
            return null;
        }
    }


    public void saveDetectionResult(Integer detectionComponentId, Object result) {
        String sql = "INSERT INTO web.IMP_DETECTION_COMPONENT " +
                "(detection_component_id, event_date, component_detection_result, create_time) " +
                "VALUES (:detectionComponentId, :eventDate, :result, CURRENT_TIMESTAMP AT TIME ZONE 'Eastern Standard Time')";

        Map<String, Object> params = new HashMap<>();
        params.put("detectionComponentId", detectionComponentId);
        params.put("eventDate", LocalDate.now());

        try {
            params.put("result", result != null ? objectMapper.writeValueAsString(result) : "Failed to detect");
            jdbcTemplate.update(sql, params);
        } catch (Exception e) {
            log.error("Error saving detection result", e);
            throw new DetectionServiceException("Failed to save detection result", e);
        }
    }

    public void saveDetectionSummary(Integer detectionId, DetectionMetaDTO detectionMeta,
                                     LocalDate eventDate, String detectionDetail, String detectionStatus) {
        Map<String, Object> matchCounts = getMatchCounts(detectionId, eventDate);
        String nearMissStatus = "Failed".equals(detectionStatus) ? "-" : determineNearMissStatus(matchCounts);

        try {
            Map<String, Object> params = createSummaryParams(detectionId, detectionMeta,
                    eventDate, detectionDetail, matchCounts, nearMissStatus, detectionStatus);
            insertDetectionSummary(params);
        } catch (Exception e) {
            log.error("Error saving detection summary", e);
            throw new DetectionServiceException("Failed to save detection summary", e);
        }
    }

    public DetectionComponentMetaDTO getDetectionCompMetaConfig(Integer detectionComponentId) {
        String sql = "SELECT * FROM web.IMP_DETECTION_COMPONENT_META WHERE detection_component_id = :detectionComponentId";
        Map<String, Object> params = Collections.singletonMap("detectionComponentId", detectionComponentId);

        try {
            return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                    DetectionComponentMetaDTO.builder()
                            .autoId(rs.getLong("auto_id"))
                            .detectionComponentId(rs.getInt("detection_component_id"))
                            .componentLabels(rs.getString("component_labels"))
                            .componentDataTypes(rs.getInt("component_data_types"))
                            .detectionComponentSource(rs.getInt("component_detection_source"))
                            .detectionComponentDescription(rs.getString("component_detection_description"))
                            .isActive(rs.getString("is_active"))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching detection comp meta configuration", e);
            return null;
        }
    }
    
    public boolean checkIncidentNo(String incidentNo) {
        String sql = "SELECT COUNT(*) FROM web.IMP_INCIDENT WHERE incident_no = :incidentNo";
        Map<String, Object> params = Collections.singletonMap("incidentNo", incidentNo);
        try {
            int count = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking incident number existence", e);
            return true;
        }
    }
    
    public boolean doneTodayDetection(Integer detectionId) {
        String sql = "SELECT COUNT(*) FROM web.IMP_DETECTION WHERE detection_id = :detectionId " +
                    "AND event_date = CAST(GETDATE() AS date) AND detection_status = 'Successful' ";
        Map<String, Object> params = new HashMap<>();
        params.put("detectionId", detectionId);

    
        int count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count > 0;    
    }
    

    
    public String getFilePatternList(Integer detectionId) {
        try {
         List<DetectionComponentMetaDTO> components = getActiveComponentDetections(detectionId);
         List<DetectionComponentMetaDTO> excelComponents = components.stream()
                 .filter(comp -> comp.getDetectionComponentSource() == 2)
                 .collect(Collectors.toList());
         
         if (excelComponents.isEmpty()) {
             log.info("No Excel components found for detection ID {}", detectionId);
             return "";
         }
         
         List<String> filePatterns = new ArrayList<>();
         for (DetectionComponentMetaDTO component : excelComponents) {
             try {
                 DetectionExcelMetaDTO excelMeta = getDetectionExcelMetaConfig(component.getDetectionComponentId());
                 if (excelMeta != null && excelMeta.getExcelFileName() != null && !excelMeta.getExcelFileName().isEmpty()) {
                     filePatterns.add(excelMeta.getExcelFileName());
                 }
             } catch (Exception e) {
                 log.warn("Error retrieving excelmetadata for component ID {}", component.getDetectionComponentId());
             }
         }
         return String.join("|", filePatterns);
        } catch (Exception e) {
             log.error("Error retrieving file pattern list for detecion ID {}", detectionId);
             return "";
        }
    }

    public void notifyFailure(Integer detectionId, Integer detectionComponentId, String errorMessage) {
        log.warn("Detection failure notification: Detection ID: {}, Component ID: {}, Error: {}",
                detectionId, detectionComponentId, errorMessage);
    }

    public Map<String, Object> getMatchCounts(Integer detectionId, LocalDate eventDate) {
        String sql = "SELECT match_result " +
                "FROM web.IMP_DETECTION_MATCH m " +
                "WHERE m.left_detection_component_id / 100 = :detectionId " +
                "AND m.event_date = :eventDate " +
                "AND m.auto_id = (SELECT MAX(auto_id) " +
                "                    FROM web.IMP_DETECTION_MATCH " +
                "                    WHERE left_detection_component_id = m.left_detection_component_id " +
                "                    AND event_date = m.event_date)";

        Map<String, Object> params = new HashMap<>();
        params.put("detectionId", detectionId);
        params.put("eventDate", eventDate);

        Map<String, Object> results = new HashMap<>();
        int toltalMisMatches = 0;
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        log.info("size of match_result {}", rows.size());
        for (Map<String, Object> row : rows) {
            String resultJson = cleanJsonString((String) row.get("match_result"));
            if (resultJson != null) {
                try {
                    JsonNode resultArray = objectMapper.readTree(resultJson);
                    if (resultArray.isArray()) {
                        toltalMisMatches += resultArray.size();
                    }
                } catch (Exception e) {
                    log.error("Failed to get mismatch count");
                }
            }
        }
        // Force OSD maximum event to 1
        if ( toltalMisMatches > 0 && (detectionId == 105 || detectionId == 106)) {
            toltalMisMatches = 1;
        }
        results.put("mismatches", toltalMisMatches);
        return results;
    }

    private String determineNearMissStatus(Map<String, Object> matchCounts) {
        int mismatches = ((Number) matchCounts.get("mismatches")).intValue();
        return String.valueOf(mismatches);
    }

    private Map<String, Object> createSummaryParams(Integer detectionId, DetectionMetaDTO detectionMeta,
                                                    LocalDate eventDate, String detectionDetail, Map<String, Object> matchCounts,
                                                    String nearMissStatus, String detectionStatus) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("eventDate", eventDate);
        params.put("nearMissStatus", nearMissStatus);
        params.put("detectionStatus", detectionStatus);
        params.put("detectionId", detectionId);
        params.put("detectionDept", detectionMeta.getDetectionDept());
        params.put("detectionDesc", detectionMeta.getDetectionDesc());
        // detectionMetaData is oboslete
        params.put("detectionMetaData", objectMapper.writeValueAsString(detectionMeta.getDetectionDept()));

        // Create metadata summary
        Map<String, Object> metaData = "-".equals(nearMissStatus) ? new HashMap<>() : createMetaDataSummary(matchCounts);
        Map<String, Object> detailMap = new HashMap<>();
        JsonNode detailJson = objectMapper.readTree(cleanJsonString(detectionDetail));
        detailMap.put("summary", metaData);
        detailMap.put("detail", detailJson);
        params.put("eventDetail", objectMapper.writeValueAsString(detailMap));

        return params;
    }

    private Map<String, Object> createMetaDataSummary(Map<String, Object> matchCounts) {
        // int totalMatches = ((Number) matchCounts.get("total")).intValue();
        int mismatches = ((Number) matchCounts.get("mismatches")).intValue();

        Map<String, Object> metaData = new HashMap<>();
        // metaData.put("totalMatches", totalMatches);
        metaData.put("mismatches", mismatches);
        // metaData.put("matchRate", totalMatches > 0 ?
        //        ((totalMatches - mismatches) * 100.0 / totalMatches) : 100);

        return metaData;
    }

    private void insertDetectionSummary(Map<String, Object> params) {
        String sql = "INSERT INTO web.IMP_DETECTION " +
                "(event_date, near_miss_status, detection_status, detection_id, " +
                "detection_dept, detection_desc, detection_meta_data, event_detail, create_time ) " +
                "VALUES (:eventDate, :nearMissStatus, :detectionStatus, :detectionId, " +
                ":detectionDept, :detectionDesc, :detectionMetaData, :eventDetail, CURRENT_TIMESTAMP AT TIME ZONE 'Eastern Standard Time')";

        jdbcTemplate.update(sql, params);
    }

    public Map<String, Object> getDetectionSummary(Integer detectionId, LocalDate eventDate) {
        String sql = "SELECT * FROM web.IMP_DETECTION " +
                "WHERE detection_id = :detectionId " +
                "AND event_date = :eventDate " +
                "AND create_time = (SELECT MAX(create_time) " +
                "                  FROM web.IMP_DETECTION " +
                "                  WHERE detection_id = :detectionId " +
                "                  AND event_date = :eventDate)";

        Map<String, Object> params = new HashMap<>();
        params.put("detectionId", detectionId);
        params.put("eventDate", eventDate);

        return jdbcTemplate.queryForMap(sql, params);
    }
    
    
    public Map<String, Object> getCurrentDaySummary(Integer detectionId) {
        List<Map<String, Object>> todayResult = getRecentDetectionSummary(0);
        Optional<Map<String, Object>>  event = todayResult.stream()
                .filter(map -> map.containsKey("detection_id") && map.get("detection_id") == detectionId).findFirst();
        return event.isPresent() ? event.get() : null;
    }

    public List<Map<String, Object>> getRecentDetectionSummary(Integer nDays) {
        // Get all detection IDs from metadata
        List<DetectionMetaDTO> detectionMetas = getAllDetectionMetas();

        String sql = "WITH latest_records AS ( " +
                "    SELECT detection_id, event_date, create_time, " +
                "           ROW_NUMBER() OVER (PARTITION BY detection_id, event_date ORDER BY create_time DESC) as rn " +
                "    FROM web.IMP_DETECTION " +
                "    WHERE  event_date BETWEEN DATEADD(day, :prevNDays, CAST(GETDATE() AS date)) " +
                "                    AND DATEADD(day, 0, CAST(GETDATE() AS date)) " +
                ") " +
                "SELECT i.auto_id ,i.event_date ,i.near_miss_status ,i.detection_status ,i.detection_id ,i.detection_dept " +
                "       ,i.detection_desc ,i.detection_meta_data ,i.event_detail " +
                "       ,FORMAT(i.create_time, 'yyyy-MM-ddTHH:mm:ss') AS create_time " +
                "FROM web.IMP_DETECTION i " +
                "INNER JOIN latest_records lr " +
                "    ON i.detection_id = lr.detection_id " +
                "    AND i.event_date = lr.event_date " +
                "    AND i.create_time = lr.create_time " +
                "WHERE lr.rn = 1";

        
        
        Map<String, Object> params = new HashMap<>();
        params.put("prevNDays", nDays);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);

        // Group results by event_date
        Map<LocalDate, Set<Integer>> dateDetectionMap = results.stream()
                .collect(Collectors.groupingBy(
                        r -> LocalDate.parse(r.get("event_date").toString()),
                        Collectors.mapping(r -> (Integer) r.get("detection_id"), Collectors.toSet())
                ));

        // Get date range from last N days to today
        LocalDate today = LocalDate.now();
        LocalDate nDaysAgo = today.minusDays(-nDays);

        // Loop through each date in range
        for (LocalDate date = nDaysAgo; !date.isAfter(today); date = date.plusDays(1)) {
            Set<Integer> detectionIdsForDate = dateDetectionMap.getOrDefault(date, new HashSet<>());

            // Add missing detection records for this date
            for (DetectionMetaDTO meta : detectionMetas) {
                if (!detectionIdsForDate.contains(meta.getDetectionId())) {
                    Map<String, Object> defaultRecord = new HashMap<>();
                    defaultRecord.put("detection_id", meta.getDetectionId());
                    defaultRecord.put("event_date", date);
                    defaultRecord.put("near_miss_status", "-");
                    defaultRecord.put("detection_status", "Not Started");
                    defaultRecord.put("detection_dept", meta.getDetectionDept());
                    defaultRecord.put("detection_desc", meta.getDetectionDesc());
                    defaultRecord.put("detection_meta_data", "{}");
                    defaultRecord.put("event_detail", "{}");
                    results.add(defaultRecord);
                }
            }
        }
        return results;
    }

    public List<Integer> getAllDetectionID() {
        String sql = "SELECT detection_id FROM web.IMP_DETECTION_META";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("detection_id"));
    }
    
    Map<String, Object> getNearMissHist() {
        LocalDate currentDate = LocalDate.now();
        Integer prevYearQuarter = (currentDate.getYear() - 1) * 100 + ((currentDate.getMonthValue() -1) /3 * 3 + 1);
        
        // Get current quarter and generate list of last 5 quarters
        String sql =  "SELECT * FROM  " + 
        "(SELECT d.detection_id, q.year_month AS time_period, " +
                "       COALESCE(h.n_of_near_miss, 0) as n_of_near_miss " +
                "FROM (SELECT DISTINCT detection_id " + 
                "      FROM web.IMP_DETECTION_MONTHLY_HIST " +
                "      WHERE detection_id IN (SELECT int_value FROM web.IMP_DETECTION_DICT WHERE dict_id = 1) " +
                "     ) d " +
                "CROSS JOIN  (" +
                " SELECT TOP 6 year_month" +
                " FROM web.IMP_DETECTION_MONTHLY_HIST" +
                " GROUP BY year_month" +
                " ORDER BY year_month DESC" +
                ")  q " +
                "LEFT JOIN web.IMP_DETECTION_MONTHLY_HIST h " +
                "    ON d.detection_id = h.detection_id " +
                "    AND q.year_month = h.year_month " +
        "UNION ALL " +
                "SELECT d.detection_id, " +
                "       (h.year_month / 100 * 100 + (( h.year_month % 100) - 1) /3 * 3 + 1) As time_period, " +
                "       SUM(COALESCE(h.n_of_near_miss, 0)) AS n_of_near_miss " +
                "FROM (SELECT DISTINCT detection_id " + 
                "      FROM web.IMP_DETECTION_MONTHLY_HIST " + 
                "      WHERE detection_id NOT IN (SELECT int_value FROM web.IMP_DETECTION_DICT WHERE dict_id = 1) " +
                "     ) d " +
                "INNER JOIN web.IMP_DETECTION_MONTHLY_HIST h " +
                "    ON d.detection_id = h.detection_id " +
                "WHERE h.year_month >= :prevYearQuarter " +
                "GROUP BY d.detection_id, (h.year_month / 100 * 100 + (( h.year_month % 100) - 1) /3 * 3 + 1) " +
        ") t ORDER BY detection_id, time_period";
        
    
        Map<String, Object> params = new HashMap<>();
        params.put("prevYearQuarter", prevYearQuarter);

        // Execute query and process results
        final Map<Integer, List<Integer>> result = new HashMap<>();

        jdbcTemplate.query(sql, params, (rs) -> {
            Integer detectionId = rs.getInt("detection_id");
            Integer nearMissCount = rs.getInt("n_of_near_miss");

            // Initialize list for detection_id if not exists and add value
            if (!result.containsKey(detectionId)) {
                result.put(detectionId, new ArrayList<>());
            }
            result.get(detectionId).add(nearMissCount);
        });
              
        Map<String, Object>    resultMap = new HashMap<>();
        resultMap.put("nearMissHistory", result);
        return resultMap;    
    }
    
    public void  upsertNearMissMonthlyHist() {
        LocalDate currentDate = LocalDate.now();
        String yearMonth = String.format("%d%02d", currentDate.getYear(), currentDate.getMonthValue());
        
        // Calculate month start and end dates
        LocalDate monthStart = LocalDate.of(currentDate.getYear(),  currentDate.getMonthValue(), 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        
        String sql =  "MERGE INTO web.IMP_DETECTION_MONTHLY_HIST As target  " + 
                "USING (SELECT :yearMonth as year_month, d.attachment_count as detection_id,   " +
                "         COUNT(0) as n_of_near_miss " +
                "       FROM web.IMP_INCIDENT d  " + 
                "       WHERE CAST(d.identification_date AS Date) BETWEEN :monthStart AND :monthEnd " +
                "       GROUP BY d.attachment_count " + 
                "       ) AS source " +
                "ON target.year_month = source.year_month AND target.detection_id = source.detection_id " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET  n_of_near_miss = source.n_of_near_miss " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT(year_month, detection_id, n_of_near_miss) " +
                "      VALUES(source.year_month, source.detection_id, source.n_of_near_miss);";
                
        
    
        Map<String, Object> params = new HashMap<>();
        params.put("yearMonth", yearMonth);
        params.put("monthStart", monthStart);
        params.put("monthEnd", monthEnd);

        jdbcTemplate.update(sql, params);
    }
}
