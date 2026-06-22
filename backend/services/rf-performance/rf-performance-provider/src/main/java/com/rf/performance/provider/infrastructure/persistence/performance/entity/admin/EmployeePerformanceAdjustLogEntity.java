package com.rf.performance.provider.infrastructure.persistence.performance.entity.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工绩效调整留痕实体。
 */
@Data
public class EmployeePerformanceAdjustLogEntity {

    /**
     * 主键编号。
     */
    private Long id;

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
     * 调整前绩效。
     */
    private String beforePerformance;

    /**
     * 调整后绩效。
     */
    private String afterPerformance;

    /**
     * 调整原因。
     */
    private String adjustReason;

    /**
     * 操作管理员 ID。
     */
    private Long operatorAdminId;

    /**
     * 操作管理员名称。
     */
    private String operatorAdminName;

    /**
     * 操作员手机号。
     */
    private String operatorMobile;

    /**
     * 操作 IP。
     */
    private String ipAddress;

    /**
     * 创建时间。
     */
    private LocalDateTime gmtCreate;
}
