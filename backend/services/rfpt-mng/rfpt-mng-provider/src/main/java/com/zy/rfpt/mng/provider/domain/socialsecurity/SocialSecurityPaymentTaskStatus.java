package com.zy.rfpt.mng.provider.domain.socialsecurity;

/**
 * 社保缴费任务状态。
 */
public enum SocialSecurityPaymentTaskStatus {

    /** 待执行。 */
    PENDING,
    /** 执行中。 */
    RUNNING,
    /** 已成功。 */
    SUCCESS,
    /** 已失败。 */
    FAILED,
    /** 已取消。 */
    CANCELED
}
