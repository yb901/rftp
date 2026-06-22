package com.rf.performance.provider.infrastructure.persistence.performance.entity.h5;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工绩效反馈实体。
 */
@Data
public class PerformanceFeedbackEntity {

    /**
     * 主键编号。
     */
    private Long id;

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 员工手机号。
     */
    private String mobile;

    /**
     * 反馈内容。
     */
    private String feedbackContent;

    /**
     * 反馈时绩效快照。
     */
    private String performanceSnapshot;

    /**
     * 提交 IP。
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent。
     */
    private String userAgent;

    /**
     * 反馈处理状态。
     */
    private String status;

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
