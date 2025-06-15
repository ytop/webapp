# database

table KRI_TASK contains all data for this application.

KRI_TASK is populated the first day of each month, with report date of the last day of previous month.
Each row either is KRI info, or Atomic Info of KRI, which depends on atomic_id, 0 is KRI, otherwise atomic
There are total 150 KRIs, 40 among them has atomics, others no atomics.
If KRI atomics, the KRI value is calculated by atomic values based on formula, otherwise KRI value was input value only.
The number  of atomics is from 2 to 8.



```
CREATE TABLE KRI_TASK (
    auto_id INT IDENTITY(1,1) PRIMARY KEY, --- auto incremant nuber
    kri_id INT NOT NULL DEFAULT 0, --- Integer from 1 to 300
    reporting_date DATETIME NOT NULL, --- The last day of month
    atomic_id INT NOT NULL DEFAULT 0, --- If it is zero, this row for KRI, otherwise , it is the index of atomic which under the KRI described by kri_id
    excel_id INT NOT NULL DEFAULT 0, --- if it is not zero, the value or atomic_value will be imported from excel file
    kri_name NVARCHAR(512),  --- the name of kri
    kri_Description NVARCHAR(4096), --- the descripttion of kri
    data_provider NVARCHAR(80) NOT NULL, --- the departments of data provider, see #1 below
    kri_owner NVARCHAR(20) NOT NULL,  --- the department of owner, see #2 below
    l1_risk_type NVARCHAR(40), --- Level 1 risk types, see #3 below
    l2_risk_type NVARCHAR(40), --- Level 2 risk types, see #4 below
    ras_metric NVARCHAR(20), --- 'RAS' or 'Non-RAS'
    warning_line NVARCHAR(255), --- The number for warning line
    limit NVARCHAR(255), --- The number for limit
    reporting_frequency NVARCHAR(40), --- Reporting frequency, 'Monthly' or 'quarterly'
    kri_value NVARCHAR(255), --- the value of KRI
    status INT DEFAULT 0, --- the status of KRI, see #5 below
    kri_value_prev_3 NVARCHAR(255), --- the kri value of 3 month ago
    kri_value_prev_2 NVARCHAR(255), --- the kri value of 2 month ago
    kri_value_prev_1 NVARCHAR(255), --- the kri value of 1 month ago
    breach_type_one_month_ago NVARCHAR(40), --- the breach type (Warning Line or Limit) of previous month
    number_breaches_last_11_month INT DEFAULT 0, --- the number of breaches between  12 month ago and 1 month ago
    breach_type NVARCHAR(40), --- breach type , (Warning Line or Limit)
    breach_remediation NVARCHAR(255) DEFAULT 'No', -- the status of breach remediation
    number_breaches_1_year INT DEFAULT 0, --- the number of breaches in last 12 months 
    breach_memo  NVARCHAR(4096), --- breach memo
    submit_time DATETIME,
    correction_time DATETIME,
    acknowledgement_time DATETIME,
    submitted_by NVARCHAR(255), -- the user id who submit the KRI value or atomic value
    approved_by NVARCHAR(255), -- the user id who approve the KRI value or atomic value
    formula NVARCHAR(511), -- the folumla to calculate KRI value based on Atomics' value
    number_atomics INT DEFAULT 0, -- the number of atomics
    atomic_metadata NVARCHAR(255), -- the description of atomic
    atomic_indicator INT DEFAULT 0, -- the index of atomix
    atomic_value NVARCHAR(255), -- the value of atomic
    atomic_status INT DEFAULT 0, -- the status of atomic, see #5 below
    submission_date DATETIME,
    rmicc_report_date DATETIME,
    ho_report_date DATETIME,
    breach_notification_date_to_ho DATETIME
);

#1 data_provider: 
data provider may be the list as, 
ADC
BKD
CBC
CBD
CDO
CHB
CISO
CLD
CRM
EO
ERM
FID
FMD
HRD
LAB
LCD
LGO
MKD
MOD
MRD
ORD
OSD
QNB
TRY
TSD

#2 kri_owner:
kri owner may be the list as, 
ADC
BKD
CBC
CBD
CDO
CHB
CISO
CLD
CRM
EO
ERM
FID
FMD
HRD
LAB
LCD
LGO
MOD
MKD
MRD
ORD
OSD
QNB
TRY
TSD

#3 Level 1 Risk Type

l1 risk type may be the list as, 

Compliance Risk
Credit Risk
Interest Rate Risk
Liquidity Risk
Operational Risk
Price Risk
Reputational Risk
Strategic Risk
Technology and Information Security

#4 Lever 2 Risk Type
l2 risk type grouped by l1 risk type, may be the list as, 
Compliance Risk --- Anti-Bribery and Anti-Corruption
Compliance Risk --- BSA/AML
Compliance Risk --- Consumer and Regulatory Compliance
Compliance Risk --- Sanctions
Credit Risk --- Concentration
Credit Risk --- Credit Default
Credit Risk --- Underwriting
Interest Rate Risk --- Repricing
Interest Rate Risk --- Basis
Interest Rate Risk --- Yield Curve
Interest Rate Risk --- Options
Liquidity Risk --- Asset/Market Liquidity
Liquidity Risk --- Contingent Liquidity
Liquidity Risk --- Funding Liquidity
Liquidity Risk --- Intraday Liquidity
Operational Risk --- Execution
Operational Risk --- Fraud
Operational Risk --- Human Capital
Operational Risk --- Legal
Operational Risk --- Model
Operational Risk --- Physical Safety
Operational Risk --- Reporting
Operational Risk --- Reporting and Corporate Governance
Operational Risk --- Third Party
Price Risk --- Banking Book
Price Risk --- Trading Book
Price Risk --- Foreign Exchange
Reputational Risk --- Brand Management 
Reputational Risk --- Relationship Management
Strategic Risk --- Business Development;Competitiveness;Geopolitical;Regulatory;ESG
Strategic Risk --- ESG
Strategic Risk --- Geopolitical
Strategic Risk --- Business Development
Strategic Risk --- Regulatory
Technology and Information Security --- Cyber
Technology and Information Security --- Data
Technology and Information Security --- Hardware
Technology and Information Security --- Physical Security
Technology and Information Security --- Software
Technology and Information Security --- Systems; Data



#5 Status and Atomic_Status

Pending Input Value: This status means the system is waiting for you to provide necessary input. Its value is 10.
Saved: This simply means your work or data has been successfully saved. Its value is 20.
Pending Input Approve: If you see this status, it indicates that the system is awaiting approval for the input you've provided. The value for this status is 30.
Adjusting: When the status is "Adjusting," it signifies that changes or modifications are currently being made to the data. This status has a value of 40.
Submitted: This status confirms that your information or request has been successfully sent. Its value is 50.
Finalized: If a task or process is "Finalized," it means it's complete and no further actions are expected. The value for this status is 60.

```
