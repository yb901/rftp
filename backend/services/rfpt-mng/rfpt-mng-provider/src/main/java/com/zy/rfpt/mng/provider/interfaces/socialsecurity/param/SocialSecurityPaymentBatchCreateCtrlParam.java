package com.zy.rfpt.mng.provider.interfaces.socialsecurity.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建社保缴费批次参数。
 */
@Data
public class SocialSecurityPaymentBatchCreateCtrlParam implements Serializable {

    /** 缴费地区编码。 */
    private String regionCode;
    /** 站点类型。 */
    private String siteType;
    /** 费款所属月份。 */
    private String periodMonth;
    /** 纳税人识别号列表。 */
    private List<String> taxNoList;
    /** 操作人。 */
    private String operator;
}
