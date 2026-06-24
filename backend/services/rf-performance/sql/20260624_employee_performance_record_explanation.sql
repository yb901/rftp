-- 员工绩效记录增加绩效说明。
-- 数据库：rf_pt

ALTER TABLE `tb_employee_performance_record`
  ADD COLUMN `performance_explanation` mediumtext COMMENT '绩效说明' AFTER `performance`;
