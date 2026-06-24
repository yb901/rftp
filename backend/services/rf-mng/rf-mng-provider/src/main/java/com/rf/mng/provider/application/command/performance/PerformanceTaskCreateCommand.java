package com.rf.mng.provider.application.command.performance;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端绩效任务创建命令。
 */
@Data
public class PerformanceTaskCreateCommand {

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
     * 创建管理员 ID。
     */
    private Long createAdminId;

    /**
     * 创建管理员名称。
     */
    private String createAdminName;
}
