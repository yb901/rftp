package com.zy.rfpt.mng.provider.application.command.socialsecurity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建社保缴费批次命令。
 */
@Data
public class SocialSecurityPaymentBatchCreateCommand implements Serializable {

    /** 缴费地区编码。 */
    private String regionCode;

    /** 站点类型。 */
    private String siteType;

    /** 费款所属月份，格式 yyyy-MM。 */
    private String periodMonth;

    /** 纳税人识别号列表；为空时按地区生成全部启用企业任务。 */
    private List<String> taxNoList;

    /** 操作人。 */
    private String operator;
}
