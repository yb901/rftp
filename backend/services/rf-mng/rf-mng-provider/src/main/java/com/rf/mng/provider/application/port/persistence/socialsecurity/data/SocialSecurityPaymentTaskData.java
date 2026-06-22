package com.rf.mng.provider.application.port.persistence.socialsecurity.data;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 社保缴费任务写入数据。
 */
@Data
public class SocialSecurityPaymentTaskData implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    private Long id;
    /** 批次编号。 */
    private Long batchId;
    /** 纳税人识别号。 */
    private String taxNo;
    /** 地区编码。 */
    private String regionCode;
    /** 站点类型，管理端触发机器人时使用。 */
    private String siteType;
    /** 费款所属月份。 */
    private String periodMonth;
    /** 任务状态。 */
    private String status;
    /** 是否可重试。 */
    private Boolean retryable;
    /** 重试次数。 */
    private Integer retryCount;
    /** 最大重试次数。 */
    private Integer maxRetryCount;
    /** 创建管理员ID。 */
    private Long createAdminId;
    /** 创建管理员名称。 */
    private String createAdminName;
}
