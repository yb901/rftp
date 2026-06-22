package com.rf.mng.provider.domain.socialsecurity;

/**
 * 社保缴费任务状态。
 */
public enum SocialSecurityPaymentTaskStatus {

    /** 执行中。 */
    PROCESSING,
    /** 已成功。 */
    SUCCESS,
    /** 已失败。 */
    FAILED,
    /** 已取消。 */
    CANCELED
}
