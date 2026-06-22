package com.rf.performance.provider.application.port.persistence.performance.record;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 绩效任务读取记录。
 */
@Data
public class PerformanceTaskRecord {

    /**
     * 绩效任务 ID。
     */
    private Long id;

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 评价周期开始日期。
     */
    private LocalDate periodStartDate;

    /**
     * 评价周期结束日期。
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
     * 任务是否删除。
     */
    private Integer isDeleted;
}
