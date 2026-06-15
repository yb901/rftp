package com.zy.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用是否标识
 *
 * @author zzy
 * @date 2026/05/19
 */
@Getter
@AllArgsConstructor
public enum YesOrNoEnum {

    /**
     * 否
     */
    NO(0, "否"),

    /**
     * 是
     */
    YES(1, "是");

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 枚举映射
     */
    private static final Map<Integer, YesOrNoEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(YesOrNoEnum::getCode, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据删除标识编码获取枚举
     *
     * @param code 删除标识编码
     * @return 枚举
     */
    public static YesOrNoEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return ENUM_MAP.get(code);
    }
}
