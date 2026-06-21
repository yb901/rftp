package com.rfpt.performance.provider.application.port.persistence.performance.data;

import lombok.Data;

/**
 * 员工绩效记录写入数据。
 */
@Data
public class EmployeePerformanceRecordData {

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

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
     * 确认状态编码。
     *
     * @see com.rfpt.performance.provider.domain.performance.PerformanceConfirmStatus
     */
    private String confirmStatus;

    /**
     * 反馈状态编码。
     *
     * @see com.rfpt.performance.provider.domain.performance.PerformanceFeedbackStatus
     */
    private String feedbackStatus;
}
