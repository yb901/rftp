package com.rf.performance.provider.infrastructure.persistence.performance.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工绩效任务实体。
 */
@Data
public class PerformanceTaskEntity {

    /**
     * 主键编号。
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
    private String status;

    /**
     * 员工绩效总数。
     */
    private Integer totalCount;

    /**
     * 确认数量。
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

    /**
     * 创建管理员 ID。
     */
    private Long createAdminId;

    /**
     * 创建管理员名称。
     */
    private String createAdminName;

    /**
     * 创建时间。
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间。
     */
    private LocalDateTime gmtModified;

    /**
     * 是否删除。
     */
    private Integer isDeleted;
}
