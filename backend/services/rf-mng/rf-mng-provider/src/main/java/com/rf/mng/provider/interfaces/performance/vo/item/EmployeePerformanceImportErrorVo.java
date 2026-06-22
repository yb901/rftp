package com.rf.mng.provider.interfaces.performance.vo.item;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效导入错误 HTTP 返回对象。
 */
@Data
public class EmployeePerformanceImportErrorVo implements Serializable {

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
