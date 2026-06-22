package com.rf.mng.provider.application.port.persistence.socialsecurity.data;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 社保缴费批次写入数据。
 */
@Data
public class SocialSecurityPaymentBatchData implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号，新增成功后由持久化层回填。 */
    private Long id;
    /** 地区编码。 */
    private String regionCode;
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
}
