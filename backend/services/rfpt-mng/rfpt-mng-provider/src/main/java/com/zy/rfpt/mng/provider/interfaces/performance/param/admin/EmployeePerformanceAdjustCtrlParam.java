package com.zy.rfpt.mng.provider.interfaces.performance.param.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效调整 HTTP 参数。
 */
@Data
public class EmployeePerformanceAdjustCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
}
