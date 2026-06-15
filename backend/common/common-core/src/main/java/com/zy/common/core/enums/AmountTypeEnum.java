package com.zy.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 面额类型
 *
 * @author zzy
 * @date 2026/05/18
 */
@Getter
@AllArgsConstructor
public enum AmountTypeEnum {

    /**
     * 固定面额
     */
    FIXED(1, "固定"),

    /**
     * 不固定面额
     */
    UNFIXED(2, "不固定");

    /**
     * 面额类型
     */
    private final Integer type;

    /**
     * 面额类型描述
     */
    private final String desc;

    /**
     * 枚举映射
     */
    private static final Map<Integer, AmountTypeEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(AmountTypeEnum::getType, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据面额类型获取枚举
     *
     * @param type 面额类型
     * @return 枚举
     */
    public static AmountTypeEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        return ENUM_MAP.get(type);
    }
}
