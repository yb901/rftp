package com.zy.common.core.enums.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 快捷支付类型枚举
 *
 * @author zzy
 * @date 2026/05/19
 */
@Getter
@AllArgsConstructor
public enum QuickPayTypeEnum {

    /**
     * 无
     */
    NONE(0, "无"),

    /**
     * 微信立减金
     */
    WX_COUPON(1, "微信立减金"),

    /**
     * 支付宝活动
     */
    ALIPAY_ACTIVITY(2, "支付宝活动");

    /**
     * 快捷支付类型
     */
    private final Integer code;

    /**
     * 快捷支付类型名称
     */
    private final String name;

    /**
     * 枚举映射
     */
    private static final Map<Integer, QuickPayTypeEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(QuickPayTypeEnum::getCode, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据数据库值获取枚举
     *
     * @param code 数据库存储值
     * @return 快捷支付类型枚举
     */
    public static QuickPayTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return ENUM_MAP.get(code);
    }
}
