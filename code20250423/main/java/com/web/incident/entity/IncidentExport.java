package com.web.incident.entity;

import com.boc.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentExport {
    private static final long serialVersionUID = -11534341125235L;
    @Excel(name = "Incident No")
    private String incidentNo;

    @Excel(name = "Incident Type")
    private String incidentType;

    @Excel(name = "Status")
    private String incidentStatus;

    @Excel(name = "Occurrence Date")
    private String occurrenceDate;

    @Excel(name = "Identification Date")
    private String identificationDate;

    @Excel(name = "Title")
    private String incidentTitle;

    @Excel(name = "Incident Identifier Department")
    private String identifiedBy;

    @Excel(name = "Incident Owner Department")
    private String ownerDepartment;

    @Excel(name = "Description")
    private String incidentDescription;

    @Excel(name = "Primary Impacted Risk Area")
    private String primaryRiskType;

    @Excel(name = "IRM Primary Owner Dept")
    private String primaryOwnerDept;

    @Excel(name = "IRM Secondary Owner Dept")
    private String secondaryOwnerDept;
}
