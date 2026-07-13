-- 社保缴费任务表索引整理。
-- 数据库：rf_robot
-- 前提：已执行 20260623_social_security_payment_task_queue.sql，且索引结构与生产库一致。
-- 说明：本脚本删除当前代码无查询路径使用的索引，保留批次汇总、税号检索和队列领取索引；
--       同时为 PROCESSING 超时心跳扫描补充组合索引。
-- 注意：DDL 会修改生产索引，请在低峰期执行一次；重复执行会因索引不存在或已存在而报错。

ALTER TABLE `tb_tax_social_security_payment_task`
  DROP INDEX `idx_tax_ss_payment_month`,
  DROP INDEX `idx_tax_ss_payment_status`,
  DROP INDEX `idx_tax_ss_payment_region`,
  DROP INDEX `idx_worker_status`,
  DROP INDEX `idx_tax_period`,
  DROP INDEX `idx_tax_ss_payment_bms_feedback`,
  ADD INDEX `idx_task_status_heartbeat` (`task_status`, `heartbeat_at`);
