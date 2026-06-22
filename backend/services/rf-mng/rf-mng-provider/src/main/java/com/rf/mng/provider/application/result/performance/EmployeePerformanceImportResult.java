package com.rf.mng.provider.application.result.performance;

import com.rf.mng.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import lombok.Data;

import java.util.List;

/**
 * 管理端员工绩效导入结果。
 */
@Data
public class EmployeePerformanceImportResult {

    /**
     * 是否导入成功。
     */
    private Boolean success;

    /**
     * 成功导入数量。
     */
    private Integer successCount;

    /**
     * 错误明细。
     */
    private List<EmployeePerformanceImportErrorResult> errors;
}
