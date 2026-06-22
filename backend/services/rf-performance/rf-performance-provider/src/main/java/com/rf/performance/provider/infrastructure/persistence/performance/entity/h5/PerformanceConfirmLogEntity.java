package com.rf.performance.provider.infrastructure.persistence.performance.entity.h5;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工绩效确认留痕实体。
 */
@Data
public class PerformanceConfirmLogEntity {

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
     * 确认类型。
     */
    private String confirmType;

    /**
     * 确认时绩效快照。
     */
    private String performanceSnapshot;

    /**
     * 短信验证留痕 ID。
     */
    private Long smsEvidenceId;

    /**
     * 短信发送业务流水号。
     */
    private String smsSendBizId;

    /**
     * 短信验证通过时间。
     */
    private LocalDateTime smsVerifiedAt;

    /**
     * 确认 IP。
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent。
     */
    private String userAgent;

    /**
     * 创建时间。
     */
    private LocalDateTime gmtCreate;
}
