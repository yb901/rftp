package com.zy.common.core.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单环境枚举
 *
 * @author zzy
 * @date 2026/06/01
 */
@Getter
@AllArgsConstructor
public enum OrderEnvironmentEnum {

    /**
     * 生产环境
     */
    PROD("prod"),

    /**
     * 测试环境
     */
    TEST("test");

    /**
     * 环境编码
     */
    private final String code;
}
