-- 员工绩效导入上传记录迁移到 OSS 文件存储。
-- 数据库：rf_pt

ALTER TABLE `tb_employee_performance_import_upload`
  ADD COLUMN `original_file_url` varchar(512) NOT NULL COMMENT '原始文件OSS访问地址' AFTER `original_content_type`,
  ADD COLUMN `failure_file_url` varchar(512) DEFAULT NULL COMMENT '失败明细文件OSS访问地址' AFTER `failure_file_name`,
  DROP COLUMN `original_file_content`,
  DROP COLUMN `failure_file_content`;
