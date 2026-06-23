-- 员工绩效导入上传记录。
-- 数据库：rf_pt

CREATE TABLE IF NOT EXISTS `tb_employee_performance_import_upload` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint unsigned NOT NULL COMMENT '绩效任务ID',
  `task_name` varchar(255) DEFAULT NULL COMMENT '绩效任务名称快照',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `original_content_type` varchar(128) DEFAULT NULL COMMENT '原始文件Content-Type',
  `original_file_url` varchar(512) NOT NULL COMMENT '原始文件OSS访问地址',
  `failure_file_name` varchar(255) DEFAULT NULL COMMENT '失败明细文件名',
  `failure_file_url` varchar(512) DEFAULT NULL COMMENT '失败明细文件OSS访问地址',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总条数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功条数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败条数',
  `status` varchar(32) NOT NULL COMMENT '导入状态：SUCCESS-成功 PARTIAL_SUCCESS-部分成功 FAILED-失败',
  `error_message` varchar(1024) DEFAULT NULL COMMENT '失败原因',
  `create_admin_id` bigint DEFAULT NULL COMMENT '创建管理员ID',
  `create_admin_name` varchar(64) DEFAULT NULL COMMENT '创建管理员名称',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_create` (`task_id`, `gmt_create`),
  KEY `idx_status_create` (`status`, `gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效导入上传记录';
