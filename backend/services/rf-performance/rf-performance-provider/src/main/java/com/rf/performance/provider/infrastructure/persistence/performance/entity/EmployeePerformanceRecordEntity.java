package com.rf.performance.provider.infrastructure.persistence.performance.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工绩效记录实体。
 */
@Data
public class EmployeePerformanceRecordEntity {

    /**
     * 主键编号。
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
     * 确认状态编码。
     */
    private String confirmStatus;

    /**
     * 反馈状态编码。
     */
    private String feedbackStatus;

    /**
     * 创建时间。
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间。
     */
    private LocalDateTime gmtModified;

    /**
     * 是否删除。
     */
    private Integer isDeleted;
}
