package com.zy.rfpt.mng.provider.application.query.socialsecurity;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社保缴费任务分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SocialSecurityPaymentTaskQuery extends PageQuery {

    /** 批次编号。 */
    private Long batchId;

    /** 纳税人识别号。 */
    private String taxNo;

    /** 任务状态。 */
    private String status;
}
