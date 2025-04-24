package com.web.mra.service.detection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.mra.dto.DetectionMatchMetaDTO;
import com.web.mra.dto.DetectionMatchResultDTO;
import com.web.mra.dto.DetectionMetaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.web.mra.service.detection.DetectionUtils.cleanJsonString;
import static com.web.mra.service.detection.DetectionUtils.createDesc;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionMatchService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final DetectionMetadataService detectionMetadataService;

    @Transactional
    public String executeMatchByDate(Integer detectionId, LocalDate eventDate) {
        validateParameters(detectionId, eventDate);

        try {
            List<DetectionMatchMetaDTO> matchRules = getActiveMatchRules(detectionId);
            return processMatchRules(matchRules, eventDate);
        } catch (Exception e) {
            String errorMsg = String.format("Match execution failed for detection ID: %d and date: %s",
                    detectionId, eventDate);
            log.error(errorMsg, e);
            throw new DetectionServiceException(errorMsg, e);
        }
    }

    private void validateParameters(Integer detectionId, LocalDate eventDate) {
        if (detectionId == null) {
            throw new IllegalArgumentException("Detection ID cannot be null");
        }
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date cannot be null");
        }
    }

    private List<DetectionMatchMetaDTO> getActiveMatchRules(Integer detectionId) {
        String sql = "SELECT * FROM web.IMP_DETECTION_MATCH_META " +
                "WHERE left_detection_component_id / 100 = :detectionId " +
                "AND is_active = 'Y' " +
                "ORDER BY auto_id";

        Map<String, Object> params = Collections.singletonMap("detectionId", detectionId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                DetectionMatchMetaDTO.builder()
                        .autoId(rs.getLong("auto_id"))
                        .leftdetectionComponentId(rs.getInt("left_detection_component_id"))
                        .rightdetectionComponentId(rs.getInt("right_detection_component_id"))
                        .matchRule(rs.getInt("match_rule"))
                        .matchThreshold(rs.getDouble("match_threshold"))
                        .isActive(rs.getString("is_active"))
                        .build()
        );
    }

    private String processMatchRules(List<DetectionMatchMetaDTO> matchRules, LocalDate eventDate) {
        List<JsonNode> allLeftValues = new ArrayList<>();

        for (DetectionMatchMetaDTO matchRule : matchRules) {
            boolean doneAll = false;
            try {
                String leftResult = detectionMetadataService.getDetectionResult(matchRule.getLeftdetectionComponentId(), eventDate);
                String rightResult = detectionMetadataService.getDetectionResult(matchRule.getRightdetectionComponentId(), eventDate);

                log.info(" leftResult in getDetectionResult : {}", leftResult);
                DetectionMatchResultDTO matchResult = compareResults(leftResult, rightResult, matchRule);
                saveMatchResult(matchRule, eventDate, matchResult);
                
                if (matchResult.getLeftValue() != null) {
                    JsonNode leftValues = objectMapper.readTree(matchResult.getLeftValue());
                    if (leftValues.isArray()) {
                        for (JsonNode value : leftValues) {
                            allLeftValues.add(value);
                        }
                    } else {
                        allLeftValues.add(leftValues);
                    }
                }
                
                // If match result is false, create incident and send email notification
                if (!matchResult.isMatches()) {
                    int detectionId =  matchRule.getLeftdetectionComponentId() / 100;
                    // Get department and description from detection metadata using component ID
                    DetectionMetaDTO detectionMeta = detectionMetadataService.getDetectionMetaConfig(detectionId);
                    String department = detectionMeta.getDetectionDept();
                    String description = detectionMeta.getDetectionDesc();

                    try {
                            log.info(" leftValue in matchResult: {}", matchResult.getLeftValue());
                            JsonNode leftValues = objectMapper.readTree(matchResult.getLeftValue());
                            if (leftValues.isArray()) {
                                for (JsonNode value : leftValues) {
                                    DetectionMatchResultDTO singleMatchResult = DetectionMatchResultDTO.builder()
                                            .matches(false)
                                            .leftValue(value.toString())
                                            .matchRule(matchResult.getMatchRule())
                                            .errorMessage(matchResult.getErrorMessage())
                                            .build();
                                    saveNearMissToIncident(matchRule, eventDate, singleMatchResult, department, description);
                                    // For detection ID 105 and 106, save only once
                                    if (detectionId == 105 || detectionId == 106) {
                                        doneAll = true;
                                        break;
                                    }
                                }
                            } else {
                                saveNearMissToIncident(matchRule, eventDate, matchResult, department, description);
                            }
                    } catch (Exception e) {
                        log.error("Error processing match rule ID: {}", matchRule.getAutoId(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing match rule ID: {}", matchRule.getAutoId(), e);
                handleMatchRuleError(matchRule, eventDate, e);
            }
            
            // OSD 105 & 106 paused once mismatched found.
            if (doneAll) {
                break;
            }
            
        }
        
        
        try {
            return objectMapper.writeValueAsString(allLeftValues);
        } catch (Exception e) {
            return "";
        }
    }

    
    private DetectionMatchResultDTO compareResults(String leftResult, String rightResult,
                                                   DetectionMatchMetaDTO matchRule) {
        if (leftResult == null || rightResult == null) {
            return createNoMatchResult("Missing detection results");
        }
        
        log.info("in compareResults: leftResult {}  -- rightResult {}", leftResult, rightResult);
        // No comparison, all found data are near miss events. Right component id is 0
        if (rightResult.trim().isEmpty()) {
            return DetectionMatchResultDTO.builder()
                    .matches((leftResult == "" || leftResult == "[]" ? true : false))
                    .leftValue(cleanJsonString(leftResult))
                    .matchRule(matchRule.getMatchRule())
                    .errorMessage(null)
                    .build();
        }


        // 
        try {
            String sanitizedLeft = cleanJsonString(leftResult);
            String sanitizedRight = cleanJsonString(rightResult);

            JsonNode leftNode = objectMapper.readTree(sanitizedLeft);
            JsonNode rightNode = objectMapper.readTree(sanitizedRight);

            if (leftNode.isEmpty()) {
                return createNoMatchResult("Empty left detection results");
            }

            // Compare all fields in the JSON objects
            boolean matches = true;
            StringBuilder leftFilterResult = new StringBuilder("[");
            for (JsonNode leftItem : leftNode) {
                boolean match = true;
                match = compareJsonNodes(leftItem, rightNode, matchRule);
                if (!match) {
                    matches = false;
                    leftFilterResult.append(objectMapper.writeValueAsString(leftItem)).append(",");
                }
            }
            
            String leftFilterResultToString = leftFilterResult.append("]").toString().replace(",]", "]");
            
            return DetectionMatchResultDTO.builder()
                    .matches(matches)
                    .leftValue(leftFilterResultToString)
                    .matchRule(matchRule.getMatchRule())
                    .errorMessage(null)
                    .build();

        } catch (Exception e) {
            log.error("Error comparing results for rule ID: {}", matchRule.getAutoId(), e);
            return createNoMatchResult("Error comparing results: " + e.getMessage());
        }
    }

    private boolean compareJsonNodes(JsonNode leftNode, JsonNode rightNode,
                                     DetectionMatchMetaDTO matchRule) {
        Iterator<String> fieldNames = rightNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode rightValue = rightNode.get(fieldName);
            
            if (!leftNode.has(fieldName)) {
                return false;
            }
            
            JsonNode leftValue = leftNode.get(fieldName);


            if (leftValue == null) {
                return false;
            }

            if (rightValue == null) {
                if (leftValue.isNumber()) {
                    // For numeric fields, apply match rule comparison
                    if (!evaluateMatchRule(leftValue.asDouble(), 0.0, matchRule)) {
                        return false;
                    }
                } else {
                    // For non-numeric fields, require exact match
                    if (!leftValue.isEmpty()) return false;
                }
            } else {
                if (leftValue.isNumber() && rightValue.isNumber()) {
                    // For numeric fields, apply match rule comparison
                    if (!evaluateMatchRule(leftValue.asDouble(), rightValue.asDouble(), matchRule)) {
                        return false;
                    }
                } else {
                    // For non-numeric fields, require exact match
                    if (!leftValue.equals(rightValue)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean evaluateMatchRule(double leftValue, double rightValue,
                                      DetectionMatchMetaDTO matchRule) {
        Double threshold = matchRule.getMatchThreshold() != null ?
                matchRule.getMatchThreshold() : 0.0;

        switch (matchRule.getMatchRule()) {
            case MatchRule.EQUAL:
                return Math.abs(leftValue - rightValue) <= threshold;
            case MatchRule.NOT_EQUAL:
                return Math.abs(leftValue - rightValue) > threshold;
            case MatchRule.GREATER_THAN:
                return leftValue > rightValue + threshold;
            case MatchRule.LESS_THAN:
                return leftValue < rightValue - threshold;
            case MatchRule.GREATER_THAN_EQUAL:
                return leftValue >= rightValue - threshold;
            case MatchRule.LESS_THAN_EQUAL:
                return leftValue <= rightValue + threshold;
            default:
                throw new IllegalArgumentException("Unsupported match rule: " + matchRule.getMatchRule());
        }
    }

    private DetectionMatchResultDTO createNoMatchResult(String errorMessage) {
        return DetectionMatchResultDTO.builder()
                .matches(false)
                .leftValue(null)
                .matchRule(null)
                .errorMessage(errorMessage)
                .build();
    }

    private void saveMatchResult(DetectionMatchMetaDTO matchRule, LocalDate eventDate,
                                 DetectionMatchResultDTO matchResult) {
        String sql = "INSERT INTO web.IMP_DETECTION_MATCH \n" +
                "(left_detection_component_id, right_detection_component_id, event_date, \n" +
                "match_summary, match_result, create_time) \n" +
                "VALUES (:leftId, :rightId, :eventDate, :summary, :result, \n" +
                "CURRENT_TIMESTAMP AT TIME ZONE 'Eastern Standard Time')";

        Map<String, Object> params = new HashMap<>();
        params.put("leftId", matchRule.getLeftdetectionComponentId());
        params.put("rightId", matchRule.getRightdetectionComponentId());
        params.put("eventDate", eventDate);
        params.put("summary", matchResult.isMatches() ? 0 : 1);

        try {
            Map<String, Object> resultDetails = createMatchResultDetails(matchResult, matchRule);
            params.put("result", objectMapper.writeValueAsString(matchResult.getLeftValue()));

            jdbcTemplate.update(sql, params);
        } catch (Exception e) {
            log.error("Error saving match result for rule ID: {}", matchRule.getAutoId(), e);
            throw new DetectionServiceException("Failed to save match result", e);
        }
    }

    private Map<String, Object> createMatchResultDetails(DetectionMatchResultDTO matchResult,
                                                         DetectionMatchMetaDTO matchRule) {
        Map<String, Object> resultDetails = new HashMap<>();
        resultDetails.put("leftValue", matchResult.getLeftValue());
        resultDetails.put("matchRule", matchResult.getMatchRule());
        resultDetails.put("matchThreshold", matchRule.getMatchThreshold());
        resultDetails.put("matches", matchResult.isMatches());

        if (matchResult.getErrorMessage() != null) {
            resultDetails.put("error", matchResult.getErrorMessage());
        }

        return resultDetails;
    }

    private void handleMatchRuleError(DetectionMatchMetaDTO matchRule, LocalDate eventDate,
                                      Exception error) {
        DetectionMatchResultDTO errorResult = createNoMatchResult(error.getMessage());
        saveMatchResult(matchRule, eventDate, errorResult);
    }

    private void saveNearMissToIncident(DetectionMatchMetaDTO matchRule, LocalDate eventDate,
                                        DetectionMatchResultDTO matchResult, String department, String description) {
        String sql = "INSERT INTO web.IMP_INCIDENT (" +
                "incident_no, incident_type, incident_status, occurrence_date, identification_date, " +
                "incident_title, identified_by, owner_department, incident_description, " +
                "risk_rating, primary_risk_type, primary_level_ii_risk, secondary_risk_type, " +
                "secondary_level_ii_risk, primary_owner_dept, secondary_owner_dept, " +
                "root_cause_category, root_cause_subcategory, root_cause_analysis, " +
                "existing_controls, due_date, attachment_count) " +
                "VALUES (" +
                ":incidentNo, :incidentType, :incidentStatus, :occurrenceDate, :identificationDate, " +
                ":incidentTitle, :identifiedBy, :ownerDepartment, :incidentDescription, " +
                ":riskRating, :primaryRiskType, :primaryLevelIiRisk, :secondaryRiskType, " +
                ":secondaryLevelIiRisk, :primaryOwnerDept, :secondaryOwnerDept, " +
                ":rootCauseCategory, :rootCauseSubcategory, :rootCauseAnalysis, " +
                ":existingControls, :dueDate, :attachmentCount)";

        // Generate random 4 characters for incident number
        int detectionId = matchRule.getLeftdetectionComponentId() / 100;
        String incidentNo = "";
        if (detectionId == 101) {
            try {
                JsonNode idNode = objectMapper.readTree(matchResult.getLeftValue()).get("ID");
                if (idNode != null) {
                    incidentNo = String.format("NM-%d-%s",
                            detectionId,
                            idNode.asText());
                }
            } catch (Exception ignored) {
                // Skip if No ID found
                return;
            }
        } else {
            String randomChars = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            incidentNo = String.format("NM-%d-%s-%s",
                detectionId,
                eventDate.format(DateTimeFormatter.BASIC_ISO_DATE),
                randomChars);
        }
        
        // Check if incidentNo 
        if (detectionMetadataService.checkIncidentNo(incidentNo)) {
            log.info("Incident Number {} already exists, skipping save incident", incidentNo);
            return;
        }
        
        String incidentDesc = "N/A";
        if (matchResult.getLeftValue() != null) {
            String descTemplate = detectionMetadataService.getDetectionDescriptionTemplate(detectionId);
            if (descTemplate.isEmpty()) {
                incidentDesc = matchResult.getLeftValue();
            } else {
                incidentDesc = createDesc(matchResult.getLeftValue(), descTemplate);
            }
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("incidentNo", incidentNo);
        params.put("incidentType", "Near Miss");
        params.put("incidentStatus", "Open");
        params.put("occurrenceDate", eventDate.getDayOfWeek() == DayOfWeek.MONDAY ?
                eventDate.minusDays(3).atStartOfDay() : eventDate.minusDays(1).atStartOfDay());
        params.put("identificationDate", eventDate.atStartOfDay());
        params.put("incidentTitle", String.format("Event Detected: %s, RDA ID: %s", description, incidentNo));
        params.put("identifiedBy", department);
        params.put("ownerDepartment", department);
        params.put("incidentDescription", incidentDesc);  // JSON details from detection component

        // Default values for required fields
        

        params.put("primaryRiskType", (detectionId == 101 ? "Technology and Information Security" : "Operational Risk"));
        params.put("secondaryRiskType", (detectionId == 101 ? "Technology and Information Security" : "Operational Risk"));
 
        params.put("primaryOwnerDept", (detectionId == 101 ? "CISO" : "ORD,ERM"));
        params.put("secondaryOwnerDept", (detectionId == 101 ? "ORD,ERM" : ""));
        
        params.put("riskRating", "Low");
        params.put("primaryLevelIiRisk", "");
        params.put("secondaryLevelIiRisk", "");
        params.put("rootCauseCategory", "");
        params.put("rootCauseSubcategory", "");
        params.put("rootCauseAnalysis", "");
        params.put("existingControls", "");
        params.put("dueDate", eventDate.plusDays(21));
        params.put("attachmentCount", detectionId);
       
        try {
            jdbcTemplate.update(sql, params);
            log.info("Successfully created near miss incident: {}", incidentNo);
        } catch (Exception e) {
            log.error("Error saving near miss incident for rule ID: {}", matchRule.getAutoId(), e);
            throw new DetectionServiceException("Failed to save near miss incident", e);
        }
    }
}
