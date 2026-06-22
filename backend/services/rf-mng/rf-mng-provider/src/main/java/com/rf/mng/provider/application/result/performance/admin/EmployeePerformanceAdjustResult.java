package com.rf.mng.provider.application.result.performance.admin;

import lombok.Data;

/**
 * 管理端员工绩效调整结果。
 */
@Data
public class EmployeePerformanceAdjustResult {

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 调整前绩效。
     */
    private String beforePerformance;

    /**
     * 调整后绩效。
     */
    private String afterPerformance;
}
