-- 社保缴费管理端第一阶段表结构。
-- 数据库：rf_pt
-- 依赖：请先在 rf_robot 执行 qy_robot 税务机器人表结构。

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
  `create_admin_id` bigint DEFAULT NULL COMMENT '创建管理员ID',
  `create_admin_name` varchar(64) DEFAULT NULL COMMENT '创建管理员名称',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_region_period_status` (`region_code`, `period_month`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='税务社保缴费批次';
