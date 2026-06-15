package com.zy.rfpt.mng.provider.domain.socialsecurity;

/**
 * 社保缴费批次状态。
 */
public enum SocialSecurityPaymentBatchStatus {

    /** 草稿。 */
    DRAFT,
    /** 已提交。 */
    SUBMITTED,
    /** 执行中。 */
    RUNNING,
    /** 部分成功。 */
    PARTIAL_SUCCESS,
    /** 全部成功。 */
    SUCCESS,
    /** 全部失败。 */
    FAILED,
    /** 已取消。 */
    CANCELED
}
