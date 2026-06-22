package com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社保缴费批次实体。
 */
@Data
public class SocialSecurityPaymentBatchEntity implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    private Long id;
    /** 地区编码。 */
    private String regionCode;
    /** 地区名称。 */
    private String regionName;
    /** 站点类型。 */
    private String siteType;
    /** 费款所属月份。 */
    private String periodMonth;
    /** 批次状态。 */
    private String status;
    /** 任务总数。 */
    private Integer totalCount;
    /** 成功数量。 */
    private Integer successCount;
    /** 失败数量。 */
    private Integer failedCount;
    /** 创建管理员ID。 */
    private Long createAdminId;
    /** 创建管理员名称。 */
    private String createAdminName;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
