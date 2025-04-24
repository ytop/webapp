package com.web.incident.dao;

import com.web.incident.entity.Incident;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface IncidentMapper {
    /**
     * Query incident list with filters
     *
     * @param map Filter parameters
     * @return List of incidents
     */
    List<Incident> selectIncidentList(Map<String, Object> map);

    /**
     * Query paginated incident list
     *
     * @param map Pagination and filter parameters
     * @return List of incidents
     */
    List<Incident> selectIncidentPage(Map<String, Object> map);


}
