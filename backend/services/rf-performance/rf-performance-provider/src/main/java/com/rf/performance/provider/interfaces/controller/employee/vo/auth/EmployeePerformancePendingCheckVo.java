package com.rf.performance.provider.interfaces.controller.employee.vo.auth;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工端待处理绩效检查结果。
 */
@Data
public class EmployeePerformancePendingCheckVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否有当前待处理绩效。
     */
    private Boolean hasPendingPerformance;
}
