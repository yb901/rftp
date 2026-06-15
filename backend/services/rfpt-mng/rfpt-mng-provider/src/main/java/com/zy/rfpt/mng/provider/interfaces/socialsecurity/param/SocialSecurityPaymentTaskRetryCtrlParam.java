package com.zy.rfpt.mng.provider.interfaces.socialsecurity.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 重试社保缴费任务参数。
 */
@Data
public class SocialSecurityPaymentTaskRetryCtrlParam implements Serializable {

    /** 操作人。 */
    private String operator;
}
