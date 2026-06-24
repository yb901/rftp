-- 放大阿里云图形验证码凭证字段，避免 captchaVerifyParam 入库超长。
ALTER TABLE `tb_employee_performance_sms_evidence`
    MODIFY COLUMN `captcha_trace_id` mediumtext COMMENT '图形验证码凭证';
