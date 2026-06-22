package com.rf.performance.provider.application.port.persistence.performance.data.admin;

import lombok.Data;

/**
 * 员工绩效调整留痕写入数据。
 */
@Data
public class EmployeePerformanceAdjustLogData {

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
}
