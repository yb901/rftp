package com.rf.performance.api.param.performance.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效调整 RPC 入参。
 */
@Data
public class EmployeePerformanceAdjustParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
