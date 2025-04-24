USE [RDA]
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION]
GO

CREATE TABLE [web].[IMP_DETECTION] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    event_date DATE NOT NULL,
    near_miss_status VARCHAR(20) NOT NULL,
    detection_status VARCHAR(20) NOT NULL,
    detection_id INT NOT NULL,
    detection_dept VARCHAR(10) NOT NULL,
    detection_desc VARCHAR(1024) NOT NULL,
    detection_meta_data VARCHAR(512) NOT NULL,
    event_detail VARCHAR(MAX),
    create_time DATETIME
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_META]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_META]
GO

CREATE TABLE [web].[IMP_DETECTION_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_id INT NOT NULL,
    detection_dept VARCHAR(10) NOT NULL,
    detection_desc VARCHAR(1024) NOT NULL
);
GO

-- ----------------------------

--     detection_component_id: the value is ( detection_id * 100 + seq_id ), detection_id is between 101 and 110
--     component_detection_source, 1 from data mart, 2 from excel, 3 from text
--     component_data_type, 1 for int, 2 for big int, 3 for  DATE  only, 4 for time to seconds,
--                           5 for decimal(...,2), 6 for decimal(...,4), 7 for decimal(...,6)
--                           8 for string
-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_COMPONENT_META]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_COMPONENT_META]
GO

CREATE TABLE [web].[IMP_DETECTION_COMPONENT_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id int NOT NULL,
    component_labels varchar(512),
    component_data_types INT NOT NULL,
    component_detection_source INT NOT NULL,
    component_detection_description VARCHAR(1024),
    is_active char(1) DEFAULT ('Y')
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_COMPONENT]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_COMPONENT]
GO

CREATE TABLE [web].[IMP_DETECTION_COMPONENT] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id int NOT NULL,
    event_date DATE NULL,
    component_detection_result varchar(MAX),
    create_time DATETIME
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_COMPONENT_CONST_META]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_COMPONENT_CONST_META]
GO

CREATE TABLE [web].[IMP_DETECTION_COMPONENT_CONST_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id int NOT NULL,
    component_detection_result varchar(1024)
);
GO

-- ----------------------------

--     match_rule, 1 for equal, 2 for negative,
--                 3 for greater than, 4 for less than
--                 5 for greater/equal than, 6 for less/equal than
-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_MATCH_META]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_MATCH_META]
GO

CREATE TABLE [web].[IMP_DETECTION_MATCH_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    left_detection_component_id int NOT NULL,
    right_detection_component_id int NOT NULL DEFAULT (0),
    match_rule int NOT NULL,
    match_threshold float,
    is_active char(1) DEFAULT ('Y')
);
GO

-- ----------------------------

--     match_summary, 0 for match,  1 for non-match
-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_MATCH]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_MATCH]
GO

CREATE TABLE [web].[IMP_DETECTION_MATCH] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    left_detection_component_id int NOT NULL,
    right_detection_component_id int NOT NULL,
    event_date DATE NULL,
    match_summary int NOT NULL,
    match_result VARCHAR(MAX),
    create_time DATETIME
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_DATA_MART_META]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_DATA_MART_META]
GO

CREATE TABLE [web].[IMP_DETECTION_DATA_MART_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id INT NOT NULL,
    sql_template VARCHAR(4096) NOT NULL,
    sql_params VARCHAR(1024)
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_EXCEL_META]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_EXCEL_META]
GO

CREATE TABLE [web].[IMP_DETECTION_EXCEL_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id INT NOT NULL,
    excel_file_name VARCHAR(255),
    sheet_name VARCHAR(255),
    cell_positions VARCHAR(4096),
    cell_params VARCHAR(4096)
);
GO



-- ----------------------------

-- ----------------------------

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_EMAIL_META]') AND type = N'U')
    DROP TABLE [web].[IMP_DETECTION_EMAIL_META]
GO

CREATE TABLE [web].[IMP_DETECTION_EMAIL_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    user_department VARCHAR(100) NOT NULL,
    email_group_bitmap INT NOT NULL,  -- 1:Data Inputter, 2:Near Miss Owner, 4:Admin
    is_active CHAR(1) DEFAULT ('Y')
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_MONTHLY_HIST]') AND type = N'U')
    DROP TABLE [web].[IMP_DETECTION_MONTHLY_HIST]
GO


CREATE TABLE [web].[IMP_DETECTION_MONTHLY_HIST] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    year_month INT NOT NULL,
    detection_id INT NOT NULL,
    n_of_near_miss INT DEFAULT (0)
);
GO


-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_DICT]') AND type = N'U')
    DROP TABLE [web].[IMP_DETECTION_DICT]
GO


CREATE TABLE [web].[IMP_DETECTION_DICT] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dict_id INT NOT NULL,
    dict_desc VARCHAR(255),
    int_value INT DEFAULT (0),
    string_value VARCHAR(512)
);
GO

-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_INCIDENT]') AND type = N'U')
DROP TABLE [web].[IMP_INCIDENT]
GO


CREATE TABLE [web].[IMP_INCIDENT] (
    -- Primary key and identifier fields
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    incident_no VARCHAR(50) NOT NULL UNIQUE,

    -- Basic incident information
    incident_type VARCHAR(50) NOT NULL CONSTRAINT CHK_IncidentType CHECK (incident_type IN ('Loss Event', 'Near Miss')),
    incident_status VARCHAR(30) NOT NULL CONSTRAINT CHK_IncidentStatus CHECK (incident_status IN ('Open', 'Pending Validation', 'Pending Owner Action', 'Pending Closure Approval', 'Closed')),
    occurrence_date DATETIME NOT NULL,
    identification_date DATETIME NOT NULL,
    incident_title VARCHAR(255) NOT NULL,
    identified_by VARCHAR(50) NOT NULL,
    owner_department VARCHAR(100) NOT NULL,
    incident_description VARCHAR(MAX) NOT NULL,

    -- Risk assessment fields
    risk_rating VARCHAR(20),
    primary_risk_type VARCHAR(50) NOT NULL,
    primary_level_ii_risk VARCHAR(50),
    secondary_risk_type VARCHAR(50),
    secondary_level_ii_risk VARCHAR(50),
    primary_owner_dept VARCHAR(100) NOT NULL,
    secondary_owner_dept VARCHAR(100),

    -- Root cause analysis
    root_cause_category VARCHAR(50),
    root_cause_subcategory VARCHAR(50),
    root_cause_analysis VARCHAR(MAX),
    existing_controls VARCHAR(MAX),
    due_date DATE,

    -- Financial impact fields (required for Loss Events)
    financial_impact DECIMAL(18,2),
    recovery_amount DECIMAL(18,2),
    recovery_date DATE,
    gl_code VARCHAR(20),
    gl_code_name VARCHAR(100),
    head_office_code VARCHAR(20),

    -- Review and challenge fields
    feedback_incident_type VARCHAR(MAX),
    feedback_incident_description VARCHAR(MAX),
    feedback_impact_description VARCHAR(MAX),
    feedback_impact_type VARCHAR(MAX),
    feedback_root_cause VARCHAR(MAX),
    feedback_existing_controls VARCHAR(MAX),
    feedback_risk_rating VARCHAR(20),
    feedback_business_identified_issue VARCHAR(MAX),

    -- Closure management
    action_taken VARCHAR(MAX),
    closure_date DATETIME,
    closure_approved_by VARCHAR(100),
    closure_approved_date DATETIME,

    -- Comments and attachments
    incident_comments VARCHAR(MAX),
    attachment_count INT DEFAULT 0,

    -- Status tracking
    review_status VARCHAR(50),
    is_business_issue BIT DEFAULT 0,

    -- Email notification tracking
    last_notification_date DATETIME,
    notification_count INT DEFAULT 0,

    -- Audit fields
    created_by VARCHAR(100) NOT NULL DEFAULT SYSTEM_USER,
    created_date DATETIME NOT NULL DEFAULT GETDATE(),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME
);

GO


-- ----------------------------

-- ----------------------------
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[web].[IMP_DETECTION_AUDIT_TRAIL]') AND type = N'U')
DROP TABLE [web].[IMP_DETECTION_AUDIT_TRAIL]
GO

CREATE TABLE [web].[IMP_DETECTION_AUDIT_TRAIL] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id  VARCHAR(80),
    action  VARCHAR(80),
    action_time DATETIME NOT NULL DEFAULT GETDATE(),
    action_description  VARCHAR(1024),
    table_name VARCHAR(100),
    field_name VARCHAR(100),
    old_value VARCHAR(MAX),
    new_value VARCHAR(MAX)
);
GO
