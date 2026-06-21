package com.rfpt.performance.provider.application.command.performance.h5;

import lombok.Data;

/**
 * 员工绩效 H5 短信发送命令。
 */
@Data
public class PerformanceH5SmsSendCommand {

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 短信场景。
     */
    private String scene;

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
}
