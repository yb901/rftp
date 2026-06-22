package com.rf.performance.api.param.performance;

import com.rf.performance.api.param.performance.item.EmployeePerformanceImportItemParam;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 员工绩效导入 RPC 入参。
 */
@Data
public class EmployeePerformanceImportParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 员工绩效明细列表。
     */
    private List<EmployeePerformanceImportItemParam> records;
}
