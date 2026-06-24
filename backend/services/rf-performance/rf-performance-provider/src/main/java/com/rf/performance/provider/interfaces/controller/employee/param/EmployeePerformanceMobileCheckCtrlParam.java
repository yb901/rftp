package com.rf.performance.provider.interfaces.controller.employee.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工端绩效手机号检查参数。
 */
@Data
public class EmployeePerformanceMobileCheckCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号。
     */
    private String mobile;
}
