package com.zy.common.core.enums.delivery.voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡券过期推送枚举
 *
 * @author zzy
 * @date 2026/06/05
 */
@Getter
@AllArgsConstructor
public enum VoucherExpireNotifyFlagEnum {

    DISABLED(0, "不推送"),
    ENABLED(1, "推送");

    private final Integer code;
    private final String name;

    public static VoucherExpireNotifyFlagEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> item.getCode().equals(code)).findFirst().orElse(null);
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
