package com.zy.rfpt.mng.provider.application.result.socialsecurity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社保缴费批次结果。
 */
@Data
public class SocialSecurityPaymentBatchResult implements Serializable {

    /** 批次编号。 */
    private Long id;

    /** 地区编码。 */
    private String regionCode;

    /** 地区名称。 */
    private String regionName;

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

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
