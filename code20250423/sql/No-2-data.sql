USE [RDA]
GO


DELETE FROM [web].[IMP_DETECTION_META];


INSERT INTO [web].[IMP_DETECTION_META] (detection_id, detection_dept, detection_desc) VALUES
(101, 'ADC', 'WP-related System Failure'),
(102, 'CLD', 'Payment Errors'),
(103, 'CLD', 'STP Rate'),
(104, 'CLD', 'Unreconciled Balance'),
(105, 'OSD', 'Security Movement Reconciliation'),
(106, 'OSD', 'Errors in Client Cash Statement');



DELETE FROM [web].[IMP_DETECTION_COMPONENT_META];


INSERT INTO [web].[IMP_DETECTION_COMPONENT_META]
(detection_component_id, component_labels, component_data_types, component_detection_source, component_detection_description, is_active) VALUES
(10101, '{"IT Event": "Description"}', 8, 2, 'Event for ADC', 'Y'),
(10201, '{"Payment Failure": "Counts"}', 8, 2, 'Payment failure for CLD', 'Y'),
(10301, '{"Reporting Date": "STP Rate"}', 6, 2, 'STP Rate for CLD', 'Y'),
(10401, '{"Account No": "Balance"}', 5, 1, 'Account Balance for CLD', 'Y'),
(10501, '{"Fed POS Movement": "Value"}', 1, 2, 'Fed POS Movement for OSD', 'Y'),
(10601, '{"Recon OSD": "Value"}', 8, 2, 'Recon for OSD', 'Y'),
(10602, '{"QDII OSD": "Value"}', 5, 2, 'QDII for OSD', 'Y')
;




DELETE FROM [web].[IMP_DETECTION_COMPONENT_CONST_META];
INSERT INTO [web].[IMP_DETECTION_COMPONENT_CONST_META] (detection_component_id,  component_detection_result) VALUES
(-1, '{"Wild-Card-Limit":0.00}'),
(-3, '{"STP_Rate":90.0}'),
(-4, '{"amount_lcy":0.0}'),
(-5, '{"WDL-SELL":0.0,"DEP+BUY":0.0,"BAL-CLOSE":0.0}'),
(-6, '{"Reconciliation result Summary":"All Matched"}'),
(-7, '{"Difference":0.0}')
 ;



DELETE FROM [web].[IMP_DETECTION_MATCH_META];

INSERT INTO [web].[IMP_DETECTION_MATCH_META]
(left_detection_component_id, right_detection_component_id, match_rule, match_threshold, is_active) VALUES
(10101, 0, 1, 0, 'Y'),
(10201, 0, 1, 0, 'Y'),
(10301, -3, 5, 0.00001, 'Y'),
(10401, -4, 1, 0.01, 'Y'),
(10501, -5, 1, 0.01, 'Y'),
(10601, -6, 1, 0, 'Y'),
(10602, -6, 1, 0, 'Y')
;



DELETE FROM [web].[IMP_DETECTION_DATA_MART_META];

INSERT INTO [web].[IMP_DETECTION_DATA_MART_META] (detection_component_id, sql_template, sql_params) VALUES
(10401,  'HardCode', '{"eventDate": "AsOfDate"}');

DELETE FROM [web].[IMP_DETECTION_EXCEL_META];

INSERT INTO [web].[IMP_DETECTION_EXCEL_META]
(detection_component_id, excel_file_name, sheet_name, cell_positions, cell_params) VALUES
(10101, 'ADCInvnetory.*', 'Sheet1', '', '{"1":{"table_range": "A1:M20"}}'),
(10201, 'GPIS NYB.*', 'Sheet1', '', '{"1":{"table_range": "B2:S9"}}'),
(10301, 'Wire Transfer STP.*', 'Sheet1', '', '{"1":{"table_range": "B12:M13"}}'),
(10401, 'Empty Dummy File', 'Empty Dummy File', '', '{"1":{"table_range": "A1:A1"}}'),
(10501, 'fed pos changes.*', 'MOVE', '', '{"1":{"table_range": "A1:J100"}}'),
(10601, '.*QDII.*', 'Cove.*', '', '{"1":{"key_range": "A5,A7,A8","value_range": "B5,C7,C8"}}'),
(10602, 'Recon.*', 'Cove.*', '', '{"1":{"key_range": "A5,A7,A8","value_range": "B5,C7,C8"}}');






DELETE FROM [web].[IMP_DETECTION_MONTHLY_HIST];


INSERT INTO [web].[IMP_DETECTION_MONTHLY_HIST] (year_month, detection_id, n_of_near_miss) VALUES
('202401', 101, 0),
('202401', 102, 0),
('202401', 103, 0),
('202401', 104, 0),
('202401', 105, 0),
('202401', 106, 0),


('202402', 101, 0),
('202402', 102, 1),
('202402', 103, 0),
('202402', 104, 0),
('202402', 105, 0),
('202402', 106, 0),

('202403', 101, 0),
('202403', 102, 0),
('202403', 103, 0),
('202403', 104, 0),
('202403', 105, 0),
('202403', 106, 0),

('202404', 101, 0),
('202404', 102, 0),
('202404', 103, 0),
('202404', 104, 0),
('202404', 105, 0),
('202404', 106, 0),

('202405', 101, 1),
('202405', 102, 1),
('202405', 103, 0),
('202405', 104, 0),
('202405', 105, 0),
('202405', 106, 0),

('202406', 101, 0),
('202406', 102, 0),
('202406', 103, 0),
('202406', 104, 0),
('202406', 105, 0),
('202406', 106, 0),

('202407', 101, 1),
('202407', 102, 0),
('202407', 103, 0),
('202407', 104, 0),
('202407', 105, 0),
('202407', 106, 0),

('202408', 101, 0),
('202408', 102, 0),
('202408', 103, 0),
('202408', 104, 0),
('202408', 105, 0),
('202408', 106, 0)
;

INSERT INTO [web].[IMP_DETECTION_MONTHLY_HIST] (year_month, detection_id, n_of_near_miss) VALUES
('202409', 101, 0),
('202409', 102, 0),
('202409', 103, 0),
('202409', 104, 0),
('202400', 105, 0),
('202409', 106, 0),

('202410', 101, 0),
('202410', 102, 0),
('202410', 103, 0),
('202410', 104, 0),
('202410', 105, 0),
('202410', 106, 2),

('202411', 101, 0),
('202411', 102, 0),
('202411', 103, 0),
('202411', 104, 0),
('202411', 105, 0),
('202411', 106, 0),

('202412', 101, 0),
('202412', 102, 1),
('202412', 103, 0),
('202412', 104, 0),
('202412', 105, 0),
('202412', 106, 0),

('202501', 101, 0),
('202501', 102, 1),
('202501', 103, 0),
('202501', 104, 0),
('202501', 105, 0),
('202501', 106, 0),

('202502', 101, 0),
('202502', 102, 1),
('202502', 103, 0),
('202502', 104, 0),
('202502', 105, 1),
('202502', 106, 0),

('202503', 101, 0),
('202503', 102, 0),
('202503', 103, 0),
('202503', 104, 0),
('202503', 105, 0),
('202503', 106, 0),

('202504', 101, 0),
('202504', 102, 0),
('202504', 103, 0),
('202504', 104, 0),
('202504', 105, 0),
('202504', 106, 0)
;

DELETE FROM [web].[IMP_DETECTION_DICT];

INSERT INTO [web].[IMP_DETECTION_DICT] (dict_id, dict_desc, int_value) VALUES
(1, 'monthly summarize', 101),
(1, 'monthly summarize', 103)
;

INSERT INTO [web].[IMP_DETECTION_DICT] (dict_id, dict_desc, int_value, string_value) VALUES
(2, 'detection description 101 template', 101, 'IT Issue with CMP ID: {ID}, Serverity: {SEVERITY}, Summary: {SUMMARY}, Soruce CI Name is {SOURCE_CI_NAME}, Source name is {SOURCE_NAME},  Occurrence first at {FIRST_OCCURRENCE} last at {LAST_OCCURRENCE}, Decison by {DECISION_BY} at {DECISION_DATE}'),
(2, 'detection description 102 template', 102, 'Error identified on a payment: Case No. {Case No.}, Amount {Amount}'),
(2, 'detection description 103 template', 103, 'Daily STP processing rate for {AsOfDate} is at {STP_Rate}%, below the 90% threshold'),
(2, 'detection description 104 template', 104, 'Account {contract_id} is detected with non-zero balance in T24 at EOD {AsOfDate}'),
(2, 'detection description 105 template', 105, 'Federal movement non-zero, CUSIP {CUSIP} with WDL-SELL {WDL-SELL} , DEP+BUY {DEP+BUY} , BAL-CLOSE {BAL-CLOSE}'),
(2, 'detection description 106 template', 106, 'Errors in reconciliation, Reconciliation result Summary {Reconciliation result Summary}')
;

INSERT INTO [web].[IMP_DETECTION_DICT] (dict_id, dict_desc, int_value, string_value) VALUES
(3, 'ORD Email Original Receiver', 1, 'ORM-IM@BOCUSA.COM'),
(3, 'ORD Email CC Receiver', 2, 'ERMISSUEMGMT@BOCUSA.COM;PaymentsRiskManagement@BOCUSA.COM'),
(3, 'ERM Admin Email Receiver', 3, 'lyan1@bocusa.com'),
(3, 'ADC Email Receiver', 4, 'LINHAN@adc.bocusa.com;RGATCHALIAN@adc.bocusa.com'),
(3, 'CLD Email Receiver', 5, 'CLD_MiddleDesk@bocusa.com;TLAM@bocusa.com;HHSIANG@bocusa.com;TTXIAO@bocusa.com'),
(3, 'OSD Email Receiver', 6, 'Custody@bocusa.com;Hccheng@bocusa.com;Xiaoyanglu@bocusa.com;MLIEF@bocusa.com')
;


DELETE FROM [web].[IMP_DETECTION_EMAIL_META];

DELETE FROM [web].[AIOS_PORTAL_MENU]
WHERE [menu_id] IN (7, 701, 702, 703, 704, 705, 706);


INSERT INTO [web].[AIOS_PORTAL_MENU] (
    [menu_id], [component], [menu_path], [menu_icon], [menu_name],
    [menu_pid], [menu_index], [menu_desc], [created_time], [updated_time],
    [method_flag], [perms], [menu_hidden], [menu_external], [no_cache], [redirect]
)
VALUES
(7, N'Layout', N'/mra', NULL, N'issueManagement', 0, 6, N'Issue Management',
    NULL, NULL, N'M', N'a', N'0', N'0', N'1', NULL),
(701, N'/mra/nearMissMonitor', N'nearMissMonitor', NULL, N'nearMissMonitor',
    7, 1, N'NearMissMonitor', NULL, NULL, N'C', N'a', N'0', N'0', N'1', NULL),
(702, N'/mra/nearMissEvent', N'nearMissEvent', NULL, N'nearMissEvent',
    7, 2, N'NearMissEvent', NULL, NULL, N'C', N'a', N'0', N'0', N'1', NULL),
(703, N'/mra/adminIncident', N'adminIncident', NULL, N'adminIncident',
    7, 3, N'AdminIncident', NULL, NULL, N'C', N'a', N'0', N'0', N'1', NULL);

GO


DELETE FROM [web].[AIOS_PORTAL_MENU_ROLE]
WHERE [menu_id] IN (7, 701, 702, 703, 704, 705, 706);

GO


--- FIDtest userid 16, role id 5 --> 1 ?
INSERT INTO [web].[AIOS_PORTAL_MENU_ROLE] ([menu_id], [role_id])
VALUES
(7, 1),
(701, 1),
(702, 1),
(703, 1);
