package com.rf.performance.provider.application.port.persistence.performance.record.admin;

import lombok.Data;

/**
 * 管理端员工绩效读取记录。
 */
@Data
public class EmployeePerformanceAdminRecord {

    /**
     * 员工绩效记录 ID。
     */
    private Long id;

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
}
