package com.web.incident.entity.request;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IncidentExportRequest {
    private String ownerDept;
    private String primaryRiskType;
    private String identificationDateFrom;
    private String identificationDateTo;
}
