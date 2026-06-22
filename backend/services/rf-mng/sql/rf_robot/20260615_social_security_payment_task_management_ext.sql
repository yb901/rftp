-- 社保缴费机器人任务管理端扩展字段。
-- 数据库：rf_robot
-- 依赖：请先执行 qy_robot/sql/014_add_tax_social_security_payment.sql。

ALTER TABLE `tb_tax_social_security_payment_task`
  ADD COLUMN `batch_id` bigint DEFAULT NULL COMMENT '管理端批次编号' AFTER `id`,
  ADD COLUMN `retryable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许重试' AFTER `error_message`,
  ADD COLUMN `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数' AFTER `retryable`,
  ADD COLUMN `max_retry_count` int NOT NULL DEFAULT 3 COMMENT '最大重试次数' AFTER `retry_count`,
  ADD COLUMN `created_by` varchar(64) DEFAULT NULL COMMENT '创建人' AFTER `max_retry_count`,
  ADD COLUMN `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人' AFTER `created_by`;

CREATE INDEX `idx_batch_status` ON `tb_tax_social_security_payment_task` (`batch_id`, `task_status`);
CREATE INDEX `idx_tax_period` ON `tb_tax_social_security_payment_task` (`tax_no`, `settle_month`);
