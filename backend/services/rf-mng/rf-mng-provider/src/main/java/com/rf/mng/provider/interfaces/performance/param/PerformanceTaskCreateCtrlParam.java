package com.rf.mng.provider.interfaces.performance.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 绩效任务创建 HTTP 参数。
 */
@Data
public class PerformanceTaskCreateCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
