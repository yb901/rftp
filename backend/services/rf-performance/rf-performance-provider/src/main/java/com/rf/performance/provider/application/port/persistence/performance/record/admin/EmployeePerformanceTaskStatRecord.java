package com.rf.performance.provider.application.port.persistence.performance.record.admin;

import lombok.Data;

/**
 * 员工绩效任务统计读取记录。
 */
@Data
public class EmployeePerformanceTaskStatRecord {

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 员工总数。
     */
    private Integer totalCount;

    /**
     * 已确认数量。
     */
    private Integer confirmedCount;

    /**
     * 反馈数量。
     */
    private Integer feedbackCount;

    /**
     * 自动确认数量。
     */
    private Integer autoConfirmedCount;
}
