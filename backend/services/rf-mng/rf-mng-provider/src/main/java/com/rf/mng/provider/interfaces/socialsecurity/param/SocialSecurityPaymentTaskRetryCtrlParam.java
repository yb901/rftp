package com.rf.mng.provider.interfaces.socialsecurity.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重试社保缴费任务参数。
 */
@Data
public class SocialSecurityPaymentTaskRetryCtrlParam implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 操作人。 */
    private String operator;
}
