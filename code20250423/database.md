# RDA Database Technical Specification

## 1. Database Overview

### 1.1 Purpose
The RDA (Risk Detection & Analysis) database implements a comprehensive system for:
- Near-miss detection and monitoring
- Incident management and tracking
- Automated notification system
- Audit logging and compliance

### 1.2 Technology Stack
- Database Engine: Microsoft SQL Server
- Schema Name: web
- Character Set: UTF-8
- Collation: SQL_Latin1_General_CP1_CI_AS

## 2. Schema Design

### 2.1 Core Detection Tables

#### 2.1.1 IMP_DETECTION
Primary table for detection records and results.

```sql
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
```

Field Descriptions:
- `near_miss_status`: Indicates if a near-miss was detected ('0', '1', '-')
- `detection_status`: Current status ('Successful', 'Failed')
- `detection_dept`: Department code ('ADC', 'CLD', 'OSD', etc.)

#### 2.1.2 IMP_DETECTION_COMPONENT
Stores individual detection component results.

```sql
CREATE TABLE [web].[IMP_DETECTION_COMPONENT] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id int NOT NULL,
    event_date DATE NULL,
    component_detection_result varchar(MAX),
    create_time DATETIME
);
```

#### 2.1.3 IMP_DETECTION_COMPONENT_META
Configuration for detection components.

```sql
CREATE TABLE [web].[IMP_DETECTION_COMPONENT_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id int NOT NULL,
    component_labels varchar(512),
    component_data_types INT NOT NULL,
    component_detection_source INT NOT NULL,
    component_detection_description VARCHAR(1024),
    is_active char(1) DEFAULT ('Y')
);
```

Data Type Mappings:
- 1: Integer
- 2: BigInt
- 3: Date
- 4: Time
- 5: Decimal(x,2)
- 6: Decimal(x,4)
- 7: Decimal(x,6)
- 8: String

### 2.2 Data Source Configuration Tables

#### 2.2.1 IMP_DETECTION_DATA_MART_META
SQL query templates for data mart sources.

```sql
CREATE TABLE [web].[IMP_DETECTION_DATA_MART_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id INT NOT NULL,
    sql_template VARCHAR(4096) NOT NULL,
    sql_params VARCHAR(1024)
);
```

#### 2.2.2 IMP_DETECTION_EXCEL_META
Excel file parsing configurations.

```sql
CREATE TABLE [web].[IMP_DETECTION_EXCEL_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    detection_component_id INT NOT NULL,
    excel_file_name VARCHAR(255),
    sheet_name VARCHAR(255),
    cell_positions VARCHAR(4096),
    cell_params VARCHAR(4096)
);
```

Example Configuration:
```sql
INSERT INTO [web].[IMP_DETECTION_EXCEL_META]
(detection_component_id, excel_file_name, sheet_name, cell_positions, cell_params) VALUES
(10101, 'ADCInvnetory.*', 'Sheet1', '', '{"1":{"table_range": "A1:M20"}}');
```

### 2.3 Match Rules

#### 2.3.1 IMP_DETECTION_MATCH_META
Defines comparison rules between components.

```sql
CREATE TABLE [web].[IMP_DETECTION_MATCH_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    left_detection_component_id int NOT NULL,
    right_detection_component_id int NOT NULL DEFAULT (0),
    match_rule int NOT NULL,
    match_threshold float,
    is_active char(1) DEFAULT ('Y')
);
```

Match Rule Types:
1. Equal
2. Negative
3. Greater than
4. Less than
5. Greater/equal than
6. Less/equal than

### 2.4 Email Notification System

#### 2.4.1 IMP_DETECTION_EMAIL_META
Email notification configurations.

```sql
CREATE TABLE [web].[IMP_DETECTION_EMAIL_META] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    user_department VARCHAR(100) NOT NULL,
    email_group_bitmap INT NOT NULL,  -- 1:Data Inputter, 2:Near Miss Owner, 4:Admin
    is_active CHAR(1) DEFAULT ('Y')
);
```

### 2.5 Historical Data

#### 2.5.1 IMP_DETECTION_MONTHLY_HIST
Monthly statistics for near-miss events.

```sql
CREATE TABLE [web].[IMP_DETECTION_MONTHLY_HIST] (
    auto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    year_month INT NOT NULL,
    detection_id INT NOT NULL,
    n_of_near_miss INT DEFAULT (0)
);
```

## 3. Data Flow and Integration

### 3.1 Detection Process Flow
1. Component data collection (SQL/Excel/Text)
2. Individual component detection
3. Match rule evaluation
4. Near-miss status determination
5. Email notification

### 3.2 Integration Points
- Data Mart connections
- File system integration for Excel files
- Email system integration
- Web application integration

## 4. Performance Optimization

### 4.1 Recommended Indexes

```sql
-- Detection lookup optimization
CREATE INDEX idx_detection_date_id 
ON [web].[IMP_DETECTION] (event_date, detection_id);

-- Component result lookup
CREATE INDEX idx_component_date 
ON [web].[IMP_DETECTION_COMPONENT] (event_date, detection_component_id);

-- Email notification lookup
CREATE INDEX idx_email_dept_active 
ON [web].[IMP_DETECTION_EMAIL_META] (user_department, is_active);
```

### 4.2 Partitioning Strategy
Consider partitioning large tables by event_date:
- IMP_DETECTION
- IMP_DETECTION_COMPONENT
- IMP_DETECTION_MATCH

## 5. Security and Audit

### 5.1 Access Control
- Schema-level security through web schema
- Application-level access control through email groups
- Regular security audit requirements

### 5.2 Audit Requirements
- Track all detection executions
- Log all near-miss identifications
- Monitor system performance
- Record email notifications

## 6. Maintenance Procedures

### 6.1 Regular Maintenance
```sql
-- Clean up old detection results
DELETE FROM [web].[IMP_DETECTION]
WHERE event_date < DATEADD(month, -6, GETDATE());

-- Archive monthly statistics
INSERT INTO [web].[IMP_DETECTION_MONTHLY_HIST]
-- ... (archive query)
```

### 6.2 Backup Strategy
- Daily full backup
- Hourly transaction log backup
- Monthly archive of historical data

## 7. Error Handling

### 7.1 Detection Errors
- Log failed detections
- Retry mechanism for transient failures
- Error notification to administrators

### 7.2 Data Validation
- Component data type validation
- Match rule validation
- Email configuration validation

## 8. Monitoring and Alerting

### 8.1 Key Metrics
- Detection success rate
- Near-miss identification rate
- System performance metrics
- Email delivery status

### 8.2 Alert Conditions
- Failed detections
- High near-miss rates
- System performance degradation
- Email delivery failures

## 9. Future Considerations

### 9.1 Scalability
- Partition strategy review
- Index optimization
- Archive strategy refinement

### 9.2 Feature Extensions
- Additional detection sources
- Enhanced matching rules
- Advanced analytics capabilities