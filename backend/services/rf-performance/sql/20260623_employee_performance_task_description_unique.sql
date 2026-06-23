-- 员工绩效任务绩效描述唯一约束。
-- 数据库：rf_pt
-- 执行前如存在重复绩效描述，需先清理重复数据：
-- SELECT performance_description, COUNT(1)
-- FROM tb_employee_performance_task
-- GROUP BY performance_description
-- HAVING COUNT(1) > 1;

ALTER TABLE `tb_employee_performance_task`
  ADD UNIQUE KEY `uk_performance_description` (`performance_description`);
