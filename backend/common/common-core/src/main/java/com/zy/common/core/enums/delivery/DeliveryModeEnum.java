package com.zy.common.core.enums.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 交付模式枚举
 *
 * @author zzy
 * @date 2026/06/05
 */
@Getter
@AllArgsConstructor
public enum DeliveryModeEnum {

    /**
     * 直接交付
     */
    DIRECT(1, "直接交付"),

    /**
     * 卡券交付
     */
    VOUCHER(2, "卡券交付");

    /**
     * 模式编码
     */
    private final Integer code;

    /**
     * 展示名称
     */
    private final String name;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举
     */
    public static DeliveryModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断编码是否合法
     *
     * @param code 编码
     * @return 是否合法
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
