package com.rf.mng.provider.application.result.performance.admin;

import lombok.Data;

/**
 * 员工绩效后台记录结果。
 */
@Data
public class EmployeePerformanceRecordResult {

    /**
     * 员工绩效记录 ID。
     */
    private Long id;

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 员工手机号。
     */
    private String mobile;

    /**
     * 员工工号。
     */
    private String employeeNo;

    /**
     * 项目或部门。
     */
    private String projectDepartment;

    /**
     * 岗位。
     */
    private String positionName;

    /**
     * 绩效。
     */
    private String performance;

    /**
     * 绩效说明。
     */
    private String performanceExplanation;

    /**
     * 确认状态编码。
     */
    private String confirmStatus;

    /**
     * 反馈状态编码。
     */
    private String feedbackStatus;

    /**
     * 反馈内容。
     */
    private String feedbackContent;

    /**
     * 反馈处理意见。
     */
    private String feedbackHandleOpinion;

    /**
     * 反馈处理人名称。
     */
    private String feedbackHandleAdminName;
}
