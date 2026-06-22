package com.rf.performance.provider.application.port.persistence.performance.data.h5;

import lombok.Data;

/**
 * 员工绩效反馈写入数据。
 */
@Data
public class PerformanceFeedbackData {

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 员工手机号。
     */
    private String mobile;

    /**
     * 反馈内容。
     */
    private String feedbackContent;

    /**
     * 反馈时绩效快照。
     */
    private String performanceSnapshot;

    /**
     * 提交 IP。
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent。
     */
    private String userAgent;

    /**
     * 反馈处理状态。
     */
    private String status;
}
