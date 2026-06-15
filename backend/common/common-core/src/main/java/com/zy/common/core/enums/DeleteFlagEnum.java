package com.zy.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用逻辑删除标识
 *
 * @author zzy
 * @date 2026/05/19
 */
@Getter
@AllArgsConstructor
public enum DeleteFlagEnum {

    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),

    /**
     * 已删除
     */
    DELETED(1, "已删除");

    /**
     * 删除标识编码
     */
    private final Integer code;

    /**
     * 删除标识描述
     */
    private final String desc;

    /**
     * 枚举映射
     */
    private static final Map<Integer, DeleteFlagEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(DeleteFlagEnum::getCode, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据删除标识编码获取枚举
     *
     * @param code 删除标识编码
     * @return 枚举
     */
    public static DeleteFlagEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return ENUM_MAP.get(code);
    }
}
