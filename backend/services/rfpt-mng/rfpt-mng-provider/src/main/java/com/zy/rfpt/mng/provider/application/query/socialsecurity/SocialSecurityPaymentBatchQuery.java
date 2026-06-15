package com.zy.rfpt.mng.provider.application.query.socialsecurity;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社保缴费批次分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SocialSecurityPaymentBatchQuery extends PageQuery {

    /** 缴费地区编码。 */
    private String regionCode;

    /** 费款所属月份。 */
    private String periodMonth;

    /** 批次状态。 */
    private String status;
}
