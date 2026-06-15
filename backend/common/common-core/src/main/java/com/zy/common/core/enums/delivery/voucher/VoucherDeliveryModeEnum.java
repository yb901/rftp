package com.zy.common.core.enums.delivery.voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡券交付模式枚举
 *
 * @author zzy
 * @date 2026/06/05
 */
@Getter
@AllArgsConstructor
public enum VoucherDeliveryModeEnum {

    ONLINE(1, "线上交付"),
    OFFLINE(2, "线下交付");

    private final Integer code;
    private final String name;

    public static VoucherDeliveryModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> item.getCode().equals(code)).findFirst().orElse(null);
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
