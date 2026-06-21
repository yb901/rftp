package com.rfpt.performance.provider.domain.performance;

import lombok.Getter;

/**
 * 绩效任务状态。
 */
@Getter
public enum PerformanceTaskStatus {

    /**
     * 草稿。
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 确认中。
     */
    CONFIRMING("CONFIRMING", "确认中"),

    /**
     * 已截止。
     */
    CLOSED("CLOSED", "已截止");

    /**
     * 状态编码。
     */
    private final String code;

    /**
     * 状态名称。
     */
    private final String name;

    /**
     * 构造绩效任务状态。
     *
     * @param code 状态编码
     * @param name 状态名称
     */
    PerformanceTaskStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
