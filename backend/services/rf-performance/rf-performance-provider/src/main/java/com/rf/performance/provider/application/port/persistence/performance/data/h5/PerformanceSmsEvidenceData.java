package com.rf.performance.provider.application.port.persistence.performance.data.h5;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工绩效短信验证留痕写入数据。
 */
@Data
public class PerformanceSmsEvidenceData {

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
}
