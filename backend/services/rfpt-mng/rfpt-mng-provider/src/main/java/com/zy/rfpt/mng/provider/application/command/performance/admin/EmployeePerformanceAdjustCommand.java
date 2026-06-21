package com.zy.rfpt.mng.provider.application.command.performance.admin;

import lombok.Data;

/**
 * 管理端员工绩效调整命令。
 */
@Data
public class EmployeePerformanceAdjustCommand {

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

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
