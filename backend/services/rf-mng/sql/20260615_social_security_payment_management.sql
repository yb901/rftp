-- 社保缴费管理端第一阶段表结构。
-- 数据库：rf_tax
-- 依赖：请先执行 qy_robot/sql/014_add_tax_social_security_payment.sql。

CREATE TABLE IF NOT EXISTS `tb_tax_social_security_payment_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `region_code` varchar(64) NOT NULL COMMENT '地区编码',
  `site_type` varchar(32) NOT NULL DEFAULT 'default' COMMENT '站点类型',
  `period_month` varchar(7) NOT NULL COMMENT '费款所属月份，yyyy-MM',
  `status` varchar(32) NOT NULL COMMENT '批次状态',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '任务总数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数量',
  `failed_count` int NOT NULL DEFAULT 0 COMMENT '失败数量',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_region_period_status` (`region_code`, `period_month`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='税务社保缴费批次';

ALTER TABLE `tb_tax_social_security_payment_task`
  ADD COLUMN `batch_id` bigint DEFAULT NULL COMMENT '管理端批次编号' AFTER `id`,
  ADD COLUMN `retryable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许重试' AFTER `error_message`,
  ADD COLUMN `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数' AFTER `retryable`,
  ADD COLUMN `max_retry_count` int NOT NULL DEFAULT 3 COMMENT '最大重试次数' AFTER `retry_count`,
  ADD COLUMN `created_by` varchar(64) DEFAULT NULL COMMENT '创建人' AFTER `max_retry_count`,
  ADD COLUMN `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人' AFTER `created_by`;

CREATE INDEX `idx_batch_status` ON `tb_tax_social_security_payment_task` (`batch_id`, `task_status`);
CREATE INDEX `idx_tax_period` ON `tb_tax_social_security_payment_task` (`tax_no`, `settle_month`);
