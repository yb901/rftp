package com.rf.performance.provider.domain.performance;

import lombok.Getter;

/**
 * 员工绩效确认状态。
 */
@Getter
public enum PerformanceConfirmStatus {

    /**
     * 待确认。
     */
    PENDING_CONFIRM("PENDING_CONFIRM", "待确认"),

    /**
     * 已确认。
     */
    CONFIRMED("CONFIRMED", "已确认"),

    /**
     * 已反馈。
     */
    FEEDBACK_SUBMITTED("FEEDBACK_SUBMITTED", "已反馈"),

    /**
     * 待二次确认。
     */
    PENDING_SECOND_CONFIRM("PENDING_SECOND_CONFIRM", "待二次确认"),

    /**
     * 二次已确认。
     */
    SECOND_CONFIRMED("SECOND_CONFIRMED", "二次已确认"),

    /**
     * 自动确认。
     */
    AUTO_CONFIRMED("AUTO_CONFIRMED", "自动确认"),

    /**
     * 二次自动确认。
     */
    SECOND_AUTO_CONFIRMED("SECOND_AUTO_CONFIRMED", "二次自动确认");

    /**
     * 状态编码。
     */
    private final String code;

    /**
     * 状态名称。
     */
    private final String name;

    /**
     * 构造确认状态。
     *
     * @param code 状态编码
     * @param name 状态名称
     */
    PerformanceConfirmStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
