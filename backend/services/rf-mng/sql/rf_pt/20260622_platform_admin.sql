-- 管理后台平台基础表结构。
-- 数据库：rf_pt

CREATE TABLE IF NOT EXISTS `tb_admin` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `real_name` varchar(50) NOT NULL COMMENT '姓名',
  `password` varchar(200) NOT NULL COMMENT '密码(SM4加密)',
  `otp_secret` varchar(200) DEFAULT NULL COMMENT 'OTP二次验证密钥',
  `enabled` smallint NOT NULL DEFAULT 1 COMMENT '是否启用：0-否 1-是',
  `role` smallint NOT NULL COMMENT '角色：1-超级管理员 2-管理员 3-运营负责人 4-商务负责人 5-运营 6-商务 7-客服 8-研发',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

