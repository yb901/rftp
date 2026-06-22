package com.rf.mng.provider.application.command.socialsecurity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建社保缴费批次命令。
 */
@Data
public class SocialSecurityPaymentBatchCreateCommand implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 缴费地区编码。 */
    private String regionCode;

    /** 站点类型。 */
    private String siteType;

    /** 费款所属月份，格式 yyyy-MM。 */
    private String periodMonth;

    /** 纳税人识别号列表；为空时按地区生成全部启用企业任务。 */
    private List<String> taxNoList;

    /** 创建管理员ID。 */
    private Long createAdminId;

    /** 创建管理员名称。 */
    private String createAdminName;
}
