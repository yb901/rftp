package com.rf.performance.provider.application.port.persistence.performance.data;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 绩效任务写入数据。
 */
@Data
public class PerformanceTaskData {

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 绩效周期开始日期。
     */
    private LocalDate periodStartDate;

    /**
     * 绩效周期结束日期。
     */
    private LocalDate periodEndDate;

    /**
     * 首次确认截止时间。
     */
    private LocalDateTime confirmDeadlineTime;

    /**
     * 二次确认截止时间。
     */
    private LocalDateTime secondConfirmDeadlineTime;

    /**
     * 任务状态编码。
     *
     * @see com.rf.performance.provider.domain.performance.PerformanceTaskStatus
     */
    private String status;

    /**
     * 创建管理员 ID。
     */
    private Long createAdminId;

    /**
     * 创建管理员名称。
     */
    private String createAdminName;
}
