package com.rf.performance.provider.application.command.performance.h5;

import lombok.Data;

/**
 * 员工绩效 H5 确认命令。
 */
@Data
public class PerformanceH5ConfirmCommand {

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 登录手机号。
     */
    private String mobile;

    /**
     * 短信验证码。
     */
    private String smsCode;

    /**
     * 请求 IP。
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent。
     */
    private String userAgent;
}
