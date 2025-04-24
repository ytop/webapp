package com.web.mra.service.detection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.mra.dto.DetectionDataMartMetaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.web.mra.service.detection.DetectionUtils.getAsOfDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionDataMartService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public Object detectDataMart(DetectionDataMartMetaDTO dataMartMeta) {
        try {
            Map<String, Object> sqlParams = parseSqlParams(dataMartMeta.getSqlParams());
            if ("HardCode".equals(dataMartMeta.getSqlTemplate())) {
                String sqlTemplate = "select  a.contract_id, b.amount_lcy from " + 
                        "(  select  contract_id, dw_contract_id from dim_contract " + 
                        "  where contract_id in ( 'USD140050001', 'USD140060001', 'CNY140060001', 'USD113080001', 'CNY113080001', 'USD140650001') " + 
                        "    and CONVERT(date, :eventDate, 112)  between effective_from_date and effective_to_date " + 
                        ") a " + 
                        " ,  fact_balance b " + 
                        "where " + 
                        " a.dw_contract_id = b.dw_contract_id " + 
                        "and b.as_of_date =:eventDate";
                List<Map<String, Object>> results = executeQuery(sqlTemplate, sqlParams);
                 if (results.isEmpty()) return "";
                 return objectMapper.writeValueAsString(results);
            } else {
                return "";
            }
        } catch (Exception e) {
            String errorMsg = String.format("Data Mart detection failed for ID: %d",
                    dataMartMeta.getDetectionComponentId());
            log.error(errorMsg, e);
            throw new DetectionServiceException(errorMsg, e);
        }
    }

    private Map<String, Object> parseSqlParams(String sqlParams) throws Exception {
        if (sqlParams == null || sqlParams.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> params = objectMapper.readValue(sqlParams, new TypeReference<Map<String, Object>>() {});
        params.entrySet().forEach(entry -> {
            if ("AsOfDate".equals(entry.getValue())) {
                entry.setValue(getAsOfDate("yyyyMMdd"));
            }
        });
        return params;
    }

    private List<Map<String, Object>> executeQuery(String sqlTemplate, Map<String, Object> params) {
        return jdbcTemplate.queryForList(sqlTemplate, params);
    }
}
