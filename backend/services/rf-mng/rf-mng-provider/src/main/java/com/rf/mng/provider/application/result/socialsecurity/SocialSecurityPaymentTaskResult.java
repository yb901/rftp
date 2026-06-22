package com.rf.mng.provider.application.result.socialsecurity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社保缴费任务结果。
 */
@Data
public class SocialSecurityPaymentTaskResult implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务编号。 */
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

    /** 是否允许重试。 */
    private Boolean retryable;

    /** 修改时间。 */
    private LocalDateTime gmtModified;
}
