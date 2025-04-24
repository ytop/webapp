// DetectionUtils.java
package com.web.mra.service.detection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing common detection-related functionality.
 */
@UtilityClass
public class DetectionUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse a JSON string into a Map.
     *
     * @param json The JSON string to parse
     * @return Parsed Map, or empty Map if input is null/empty
     */
    public static Map<String, Object> parseJsonToMap(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return Collections.emptyMap();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new DetectionServiceException("Error parsing JSON to Map", e);
        }
    }

    /**
     * Parse a JSON string into a List of strings.
     *
     * @param json The JSON string to parse
     * @return Parsed List, or empty List if input is null/empty
     */
    public static List<String> parseJsonToStringList(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new DetectionServiceException("Error parsing JSON to List", e);
        }
    }

    /**
     * Convert an object to JSON string.
     *
     * @param obj The object to convert
     * @return JSON string representation
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new DetectionServiceException("Error converting object to JSON", e);
        }
    }

    public static String cleanJsonString(String inputA) {
        if (inputA == null || inputA.isEmpty()) {
            return inputA;
        }

        String input = inputA;

        // Remove beginning and ending double quotes
        if (input.length() >= 2 && input.charAt(0) == '"' && input.charAt(input.length() - 1) == '"') {
            input = input.substring(1, input.length() - 1);
        }

        // Remove backslashes
        return input.replace("\\", "");
    }

    public static String getAsOfDate(String dateFormat) {
        LocalDate today = LocalDate.now();
        LocalDate resultDate;
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            resultDate = LocalDate.now().minusDays(3);
        } else {
            resultDate = LocalDate.now().minusDays(1);
        }
        return resultDate.format(DateTimeFormatter.ofPattern(dateFormat));
    }
    
    public static String createDesc(String dataJson, String descTemplate) {
        try {
            String result = descTemplate;
            Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(result);
            JsonNode jsonNode = objectMapper.readTree(dataJson);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String fieldName = matcher.group(1);
                if (fieldName.equals("AsOfDate")) {
                    String fieldValue = getAsOfDate("M/d/yyyy");
                    matcher.appendReplacement(sb, fieldValue);
                    continue;
                }
                JsonNode fieldValueNode = jsonNode.get(fieldName);
                if (fieldValueNode != null) {
                    String fieldValue = fieldValueNode.asText();
                    if (isDate(fieldValue)) {
                        fieldValue = formatDate(fieldValue);
                    }
                    matcher.appendReplacement(sb, fieldValue);
                } else {
                    matcher.appendReplacement(sb, "(N/A)");
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isDate(String str) {
        if (str == null || str.length() != 8) {
            return false;
        }

        try {
            int year = Integer.parseInt(str.substring(0, 4));
            if (year < 2020 || year > 2099) {
                return false;
            }

            Integer.parseInt(str);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(false);
            sdf.parse(str);
            return true;
        } catch (NumberFormatException | ParseException e) {
            return false;
        }
    }

    private static String formatDate(String dateStr) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("M/d/yyyy");
        Date date = inputFormat.parse(dateStr);
        return outputFormat.format(date);
    }
}
