package com.zy.rfpt.mng.provider.application.result.performance.item;

import lombok.Data;

/**
 * 管理端员工绩效导入错误明细。
 */
@Data
public class EmployeePerformanceImportErrorResult {

    /**
     * 导入行号。
     */
    private Integer rowNo;

    /**
     * 员工手机号。
     */
    private String mobile;

    /**
     * 错误原因。
     */
    private String errorMessage;
}
