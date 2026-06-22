package com.rf.mng.provider.application.result.performance;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端绩效任务返回对象。
 */
@Data
public class PerformanceTaskResult {

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
     */
    private String statusCode;

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
