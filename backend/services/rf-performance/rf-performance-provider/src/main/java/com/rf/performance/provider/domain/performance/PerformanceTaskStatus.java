package com.rf.performance.provider.domain.performance;

import lombok.Getter;

/**
 * 绩效任务状态。
 */
@Getter
public enum PerformanceTaskStatus {

    /**
     * 开启，员工端可查看和确认。
     */
    OPEN("OPEN", "开启"),

    /**
     * 关闭，员工端不可见。
     */
    CLOSED("CLOSED", "关闭");

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

    /**
     * 判断任务状态是否为开启。
     *
     * @param status 状态编码
     * @return 是否开启
     */
    public static boolean isOpen(String status) {
        return OPEN.code.equals(status) || "CONFIRMING".equals(status);
    }

    /**
     * 判断任务状态是否为关闭。
     *
     * @param status 状态编码
     * @return 是否关闭
     */
    public static boolean isClosed(String status) {
        return CLOSED.code.equals(status) || "DRAFT".equals(status);
    }
}
