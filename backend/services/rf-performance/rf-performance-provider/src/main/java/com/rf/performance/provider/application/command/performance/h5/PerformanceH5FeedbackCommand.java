package com.rf.performance.provider.application.command.performance.h5;

import lombok.Data;

/**
 * 员工绩效 H5 反馈命令。
 */
@Data
public class PerformanceH5FeedbackCommand {

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 登录手机号。
     */
    private String mobile;

    /**
     * 反馈内容。
     */
    private String feedbackContent;

    /**
     * 请求 IP。
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent。
     */
    private String userAgent;
}
