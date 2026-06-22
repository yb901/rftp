package com.rf.performance.provider.infrastructure.persistence.performance.entity.h5;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工绩效短信验证留痕实体。
 */
@Data
public class PerformanceSmsEvidenceEntity {

    /**
     * 主键编号。
     */
    private Long id;

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 短信场景。
     */
    private String scene;

    /**
     * 短信验证码。
     */
    private String smsCode;

    /**
     * 短信发送业务流水号。
     */
    private String smsSendBizId;

    /**
     * 图形验证码凭证。
     */
    private String captchaTraceId;

    /**
     * 请求 IP。
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent。
     */
    private String userAgent;

    /**
     * 发送时间。
     */
    private LocalDateTime sentAt;

    /**
     * 验证通过时间。
     */
    private LocalDateTime verifiedAt;

    /**
     * 创建时间。
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间。
     */
    private LocalDateTime gmtModified;
}
