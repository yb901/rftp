package com.zy.rfpt.mng.provider.interfaces.performance.param;

import com.zy.rfpt.mng.provider.interfaces.performance.param.item.EmployeePerformanceImportItemCtrlParam;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 员工绩效导入 HTTP 参数。
 */
@Data
public class EmployeePerformanceImportCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 员工绩效明细列表。
     */
    private List<EmployeePerformanceImportItemCtrlParam> records;
}
