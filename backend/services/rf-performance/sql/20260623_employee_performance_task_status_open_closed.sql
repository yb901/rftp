-- 员工绩效任务状态调整为开启/关闭。
-- 数据库：rf_pt

ALTER TABLE `tb_employee_performance_task`
  MODIFY COLUMN `status` varchar(32) NOT NULL DEFAULT 'CLOSED' COMMENT '任务状态：OPEN-开启，CLOSED-关闭';

UPDATE `tb_employee_performance_task`
SET `status` = 'CLOSED'
WHERE `status` = 'DRAFT';

UPDATE `tb_employee_performance_task`
SET `status` = 'OPEN'
WHERE `status` = 'CONFIRMING';
