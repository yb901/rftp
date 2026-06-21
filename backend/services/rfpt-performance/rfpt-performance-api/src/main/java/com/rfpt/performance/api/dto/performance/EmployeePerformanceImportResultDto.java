package com.rfpt.performance.api.dto.performance;

import com.rfpt.performance.api.dto.performance.item.EmployeePerformanceImportErrorDto;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 员工绩效导入 RPC 返回对象。
 */
@Data
public class EmployeePerformanceImportResultDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    private List<EmployeePerformanceImportErrorDto> errors;
}
