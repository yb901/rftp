package com.rf.performance.api.dto.performance.item;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效导入错误明细。
 */
@Data
public class EmployeePerformanceImportErrorDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
