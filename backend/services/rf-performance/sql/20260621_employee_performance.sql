-- 员工绩效查看与确认反馈表结构。
-- 数据库：rf_pt

CREATE TABLE IF NOT EXISTS `tb_employee_performance_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `performance_description` varchar(255) NOT NULL COMMENT '绩效描述',
  `period_start_date` date NOT NULL COMMENT '评价周期开始日期',
  `period_end_date` date NOT NULL COMMENT '评价周期结束日期',
  `confirm_deadline_time` datetime NOT NULL COMMENT '首次确认截止时间',
  `second_confirm_deadline_time` datetime DEFAULT NULL COMMENT '二次确认截止时间',
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '任务状态',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '员工绩效总数',
  `confirmed_count` int NOT NULL DEFAULT 0 COMMENT '确认数量',
  `feedback_count` int NOT NULL DEFAULT 0 COMMENT '反馈数量',
  `auto_confirmed_count` int NOT NULL DEFAULT 0 COMMENT '自动确认数量',
  `create_admin_id` bigint DEFAULT NULL COMMENT '创建管理员ID',
  `create_admin_name` varchar(64) DEFAULT NULL COMMENT '创建管理员名称',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_period` (`period_start_date`, `period_end_date`),
  KEY `idx_status_deadline` (`status`, `confirm_deadline_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效任务';

CREATE TABLE IF NOT EXISTS `tb_employee_performance_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `task_id` bigint NOT NULL COMMENT '绩效任务ID',
  `employee_name` varchar(64) NOT NULL COMMENT '员工姓名',
  `mobile` varchar(32) NOT NULL COMMENT '员工手机号',
  `employee_no` varchar(64) DEFAULT NULL COMMENT '员工工号',
  `project_department` varchar(128) DEFAULT NULL COMMENT '项目或部门',
  `position_name` varchar(128) DEFAULT NULL COMMENT '岗位',
  `performance` varchar(128) NOT NULL COMMENT '绩效',
  `confirm_status` varchar(32) NOT NULL DEFAULT 'PENDING_CONFIRM' COMMENT '确认状态',
  `feedback_status` varchar(32) NOT NULL DEFAULT 'NONE' COMMENT '反馈状态',
  `last_confirm_time` datetime DEFAULT NULL COMMENT '最近确认时间',
  `last_feedback_time` datetime DEFAULT NULL COMMENT '最近反馈时间',
  `last_adjust_time` datetime DEFAULT NULL COMMENT '最近调整时间',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_mobile` (`task_id`, `mobile`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_task_status` (`task_id`, `confirm_status`, `feedback_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效记录';

CREATE TABLE IF NOT EXISTS `tb_employee_performance_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `task_id` bigint NOT NULL COMMENT '绩效任务ID',
  `record_id` bigint NOT NULL COMMENT '员工绩效记录ID',
  `mobile` varchar(32) NOT NULL COMMENT '员工手机号',
  `feedback_content` varchar(2000) NOT NULL COMMENT '反馈内容',
  `performance_snapshot` varchar(128) NOT NULL COMMENT '反馈时绩效快照',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '提交IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '浏览器User-Agent',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '反馈处理状态',
  `handle_opinion` varchar(1000) DEFAULT NULL COMMENT '处理意见',
  `handle_admin_id` bigint DEFAULT NULL COMMENT '处理管理员ID',
  `handle_admin_name` varchar(64) DEFAULT NULL COMMENT '处理管理员名称',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_feedback` (`record_id`),
  KEY `idx_task_status` (`task_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效反馈';

CREATE TABLE IF NOT EXISTS `tb_employee_performance_adjust_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `task_id` bigint NOT NULL COMMENT '绩效任务ID',
  `record_id` bigint NOT NULL COMMENT '员工绩效记录ID',
  `mobile` varchar(32) NOT NULL COMMENT '员工手机号',
  `before_performance` varchar(128) NOT NULL COMMENT '调整前绩效',
  `after_performance` varchar(128) NOT NULL COMMENT '调整后绩效',
  `adjust_reason` varchar(1000) DEFAULT NULL COMMENT '调整原因',
  `operator_admin_id` bigint DEFAULT NULL COMMENT '操作管理员ID',
  `operator_admin_name` varchar(64) DEFAULT NULL COMMENT '操作管理员名称',
  `operator_mobile` varchar(32) DEFAULT NULL COMMENT '操作员手机号',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '操作IP',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效调整留痕';

CREATE TABLE IF NOT EXISTS `tb_employee_performance_confirm_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `task_id` bigint NOT NULL COMMENT '绩效任务ID',
  `record_id` bigint NOT NULL COMMENT '员工绩效记录ID',
  `mobile` varchar(32) NOT NULL COMMENT '员工手机号',
  `confirm_type` varchar(32) NOT NULL COMMENT '确认类型',
  `performance_snapshot` varchar(128) NOT NULL COMMENT '确认时绩效快照',
  `sms_evidence_id` bigint DEFAULT NULL COMMENT '短信验证留痕ID',
  `sms_send_biz_id` varchar(128) DEFAULT NULL COMMENT '短信发送业务流水号',
  `sms_verified_at` datetime DEFAULT NULL COMMENT '短信验证通过时间',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '确认IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '浏览器User-Agent',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_type` (`record_id`, `confirm_type`),
  KEY `idx_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效确认留痕';

CREATE TABLE IF NOT EXISTS `tb_employee_performance_sms_evidence` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `mobile` varchar(32) NOT NULL COMMENT '手机号',
  `scene` varchar(32) NOT NULL COMMENT '短信场景',
  `sms_code` varchar(16) DEFAULT NULL COMMENT '短信验证码',
  `sms_send_biz_id` varchar(128) DEFAULT NULL COMMENT '短信发送业务流水号',
  `captcha_trace_id` varchar(128) DEFAULT NULL COMMENT '图形验证码凭证',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '请求IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '浏览器User-Agent',
  `sent_at` datetime DEFAULT NULL COMMENT '发送时间',
  `verified_at` datetime DEFAULT NULL COMMENT '验证通过时间',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_mobile_scene` (`mobile`, `scene`),
  KEY `idx_sms_send_biz_id` (`sms_send_biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工绩效短信验证留痕';
