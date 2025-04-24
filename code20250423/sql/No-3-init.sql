USE [RDA]
GO


DELETE FROM [web].[IMP_DETECTION];


INSERT INTO [web].[IMP_DETECTION] ( event_date,near_miss_status , detection_status , detection_id ,  detection_dept ,    detection_desc ,
    detection_meta_data ,    event_detail ,  create_time ) VALUES
('2025-04-30', '0', 'Successful',101, 'ADC', 'WP-related System Failure', 'ADC', 'WP-related System Failure', '2025-04-11 13:45:00'),
('2025-04-30', '0', 'Successful',102, 'CLD', 'Payment Errors', 'CLD', 'Payment Errors', '2025-04-11 13:45:00'),
('2025-04-30', '0', 'Successful',103, 'CLD', 'STP Rate', 'CLD', 'STP Rate', '2025-04-11 13:45:00'),
('2025-04-30', '0', 'Successful',104, 'CLD', 'Unreconciled Balance', 'CLD', 'Unreconciled Balance', '2025-04-11 13:45:00'),
('2025-04-30', '0', 'Successful',105, 'OSD', 'Security Movement Reconciliation', 'OSD', 'Security Movement Reconciliation', '2025-04-11 13:45:00'),
('2025-04-30', '0', 'Successful',106, 'OSD', 'Errors in Client Cash Statement', 'OSD', 'Errors in Client Cash Statement', '2025-04-11 13:45:00');

