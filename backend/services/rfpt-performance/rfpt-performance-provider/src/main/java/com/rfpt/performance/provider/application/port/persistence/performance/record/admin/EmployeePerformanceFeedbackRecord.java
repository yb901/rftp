package com.rfpt.performance.provider.application.port.persistence.performance.record.admin;

import lombok.Data;

/**
 * 员工绩效反馈记录。
 */
@Data
public class EmployeePerformanceFeedbackRecord {

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 反馈内容。
     */
    private String feedbackContent;

    /**
     * 处理意见。
     */
    private String handleOpinion;

    /**
     * 处理管理员名称。
     */
    private String handleAdminName;
}
