package com.rf.performance.provider.domain.performance;

import lombok.Getter;

/**
 * 员工绩效反馈状态。
 */
@Getter
public enum PerformanceFeedbackStatus {

    /**
     * 无反馈。
     */
    NONE("NONE", "无反馈"),

    /**
     * 待处理。
     */
    PENDING("PENDING", "待处理"),

    /**
     * 已处理且已调整。
     */
    HANDLED_ADJUSTED("HANDLED_ADJUSTED", "已处理-已调整"),

    /**
     * 已处理且未调整。
     */
    HANDLED_UNCHANGED("HANDLED_UNCHANGED", "已处理-未调整");

    /**
     * 状态编码。
     */
    private final String code;

    /**
     * 状态名称。
     */
    private final String name;

    /**
     * 构造反馈状态。
     *
     * @param code 状态编码
     * @param name 状态名称
     */
    PerformanceFeedbackStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
