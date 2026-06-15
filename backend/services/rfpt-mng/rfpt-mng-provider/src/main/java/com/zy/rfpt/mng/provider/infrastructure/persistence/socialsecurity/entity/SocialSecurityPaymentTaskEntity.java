package com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社保缴费任务实体。
 */
@Data
public class SocialSecurityPaymentTaskEntity implements Serializable {

    /** 主键编号。 */
    private Long id;
    /** 批次编号。 */
    private Long batchId;
    /** 纳税人识别号。 */
    private String taxNo;
    /** 企业名称。 */
    private String enterpriseName;
    /** 社保账号名称。 */
    private String securityAccountName;
    /** 地区编码。 */
    private String regionCode;
    /** 站点类型。 */
    private String siteType;
    /** 费款所属月份。 */
    private String periodMonth;
    /** 任务状态。 */
    private String status;
    /** 应缴总额。 */
    private BigDecimal payableAmount;
    /** 失败编码。 */
    private String errorCode;
    /** 失败原因。 */
    private String errorMessage;
    /** 是否可重试。 */
    private Boolean retryable;
    /** 重试次数。 */
    private Integer retryCount;
    /** 最大重试次数。 */
    private Integer maxRetryCount;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
