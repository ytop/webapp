package com.web.incident.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boc.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("web.IMP_INCIDENT")
public class Incident {
    @TableId("auto_id")
    @Excel(name = "ID")
    private Long autoId;


    @TableField("incident_no")
    @Excel(name = "Incident No")
    private String incidentNo;

    @TableField("incident_type")
    @Excel(name = "Incident Type")
    private String incidentType;

    @TableField("incident_status")
    @Excel(name = "Status")
    private String incidentStatus;

    @TableField("occurrence_date")
    @Excel(name = "Occurrence Date")
    private Date occurrenceDate;

    @TableField("identification_date")
    @Excel(name = "Identification Date")
    private Date identificationDate;

    @TableField("incident_title")
    @Excel(name = "Title")
    private String incidentTitle;

    @TableField("identified_by")
    @Excel(name = "Incident Identifier Department")
    private String identifiedBy;

    @TableField("owner_department")
    @Excel(name = "Incident Owner Department")
    private String ownerDepartment;

    @TableField("incident_description")
    @Excel(name = "Description")
    private String incidentDescription;

    @TableField("risk_rating")
    @Excel(name = "Risk Rating")
    private String riskRating;

    @TableField("primary_risk_type")
    @Excel(name = "Primary Impacted Risk Area")
    private String primaryRiskType;

    @TableField("primary_level_ii_risk")
    @Excel(name = "Primary Level II Risk")
    private String primaryLevelIiRisk;

    @TableField("secondary_risk_type")
    @Excel(name = "Secondary Risk Type")
    private String secondaryRiskType;

    @TableField("secondary_level_ii_risk")
    @Excel(name = "Secondary Level II Risk")
    private String secondaryLevelIiRisk;

    @TableField("primary_owner_dept")
    @Excel(name = "IRM Primary Owner Dept")
    private String primaryOwnerDept;

    @TableField("secondary_owner_dept")
    @Excel(name = "IRM Secondary Owner Dept")
    private String secondaryOwnerDept;

    @TableField("root_cause_category")
    @Excel(name = "Root Cause Category")
    private String rootCauseCategory;

    @TableField("root_cause_subcategory")
    @Excel(name = "Root Cause Subcategory")
    private String rootCauseSubcategory;

    @TableField("root_cause_analysis")
    @Excel(name = "Root Cause Analysis")
    private String rootCauseAnalysis;

    @TableField("existing_controls")
    @Excel(name = "Existing Controls")
    private String existingControls;

    @TableField("due_date")
    @Excel(name = "Due Date")
    private Date dueDate;

    @TableField("financial_impact")
    @Excel(name = "Financial Impact")
    private BigDecimal financialImpact;

    @TableField("recovery_amount")
    @Excel(name = "Recovery Amount")
    private BigDecimal recoveryAmount;

    @TableField("recovery_date")
    @Excel(name = "Recovery Date")
    private Date recoveryDate;

    @TableField("gl_code")
    @Excel(name = "GL Code")
    private String glCode;

    @TableField("gl_code_name")
    @Excel(name = "GL Code Name")
    private String glCodeName;

    @TableField("head_office_code")
    @Excel(name = "Head Office Code")
    private String headOfficeCode;

    @TableField("incident_comments")
    @Excel(name = "Comments")
    private String incidentComments;

    // Adding missing feedback fields
    @TableField("feedback_incident_type")
    @Excel(name = "Feedback Incident Type")
    private String feedbackIncidentType;

    @TableField("feedback_incident_description")
    @Excel(name = "Feedback Incident Description")
    private String feedbackIncidentDescription;

    @TableField("feedback_impact_description")
    @Excel(name = "Feedback Impact Description")
    private String feedbackImpactDescription;

    @TableField("feedback_impact_type")
    @Excel(name = "Feedback Impact Type")
    private String feedbackImpactType;

    @TableField("feedback_root_cause")
    @Excel(name = "Feedback Root Cause")
    private String feedbackRootCause;

    @TableField("feedback_existing_controls")
    @Excel(name = "Feedback Existing Controls")
    private String feedbackExistingControls;

    @TableField("feedback_risk_rating")
    @Excel(name = "Feedback Risk Rating")
    private String feedbackRiskRating;

    @TableField("feedback_business_identified_issue")
    @Excel(name = "Feedback Business Identified Issue")
    private String feedbackBusinessIdentifiedIssue;

    // Closure management fields
    @TableField("action_taken")
    @Excel(name = "Action Taken")
    private String actionTaken;

    @TableField("closure_date")
    @Excel(name = "Closure Date")
    private Date closureDate;

    @TableField("closure_approved_by")
    @Excel(name = "Closure Approved By")
    private String closureApprovedBy;

    @TableField("closure_approved_date")
    @Excel(name = "Closure Approved Date")
    private Date closureApprovedDate;

    // Status tracking fields
    @TableField("attachment_count")
    @Excel(name = "Attachment Count")
    private Integer attachmentCount;

    @TableField("review_status")
    @Excel(name = "Review Status")
    private String reviewStatus;

    @TableField("is_business_issue")
    @Excel(name = "Is Business Issue")
    private Boolean isBusinessIssue;

    // Email notification tracking fields
    @TableField("last_notification_date")
    @Excel(name = "Last Notification Date")
    private Date lastNotificationDate;

    @TableField("notification_count")
    @Excel(name = "Notification Count")
    private Integer notificationCount;

    // Audit fields
    @TableField("created_by")
    @Excel(name = "Created By")
    private String createdBy;

    @TableField("created_date")
    @Excel(name = "Created Date")
    private Date createdDate;

    @TableField("last_modified_by")
    @Excel(name = "Last Modified By")
    private String lastModifiedBy;

    @TableField("last_modified_date")
    @Excel(name = "Last Modified Date")
    private Date lastModifiedDate;
}
