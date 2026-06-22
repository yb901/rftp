package com.rf.performance.provider.application.result.performance.h5;

import lombok.Data;

/**
 * 员工绩效 H5 返回对象。
 */
@Data
public class EmployeePerformanceH5Result {

    /**
     * 员工绩效记录 ID。
     */
    private Long id;

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 评价周期展示文本。
     */
    private String periodText;

    /**
     * 绩效。
     */
    private String performance;

    /**
     * 确认状态编码。
     */
    private String confirmStatus;

    /**
     * 确认状态展示文本。
     */
    private String confirmStatusText;

    /**
     * 反馈状态编码。
     */
    private String feedbackStatus;

    /**
     * 确认截止时间展示文本。
     */
    private String confirmDeadlineTime;
}
