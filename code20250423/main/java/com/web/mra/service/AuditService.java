package com.web.mra.service;

import com.boc.utils.SecurityUtils;
import com.web.sys.entity.AiosPortalUser;
import com.web.sys.entity.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void logAudit(String userId, String action, String description,
            String tableName, String fieldName, String oldValue, String newValue) {
        String sql = " INSERT INTO web.IMP_DETECTION_AUDIT_TRAIL " +
                "(user_id, action, action_description, table_name, field_name, old_value, new_value) " +
                "VALUES (:userId, :action, :description, :tableName, :fieldName, :oldValue, :newVaule)";
        
        Map<String, Object> params = new HashMap<>();
        if (userId == null) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            AiosPortalUser user = loginUser.getUser();
            userId = user.getUserName();
        }
        params.put("userId", userId);
        params.put("action", action);
        params.put("description", description);
        params.put("tableName", tableName);
        params.put("fieldName", fieldName);
        params.put("oldValue", oldValue);
        params.put("newVaule", newValue);
       
        
        jdbcTemplate.update(sql, params);
    }
}
