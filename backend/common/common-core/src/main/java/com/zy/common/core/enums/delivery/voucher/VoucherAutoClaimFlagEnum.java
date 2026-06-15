package com.zy.common.core.enums.delivery.voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡券自动领取枚举
 *
 * @author zzy
 * @date 2026/06/05
 */
@Getter
@AllArgsConstructor
public enum VoucherAutoClaimFlagEnum {

    DISABLED(0, "不自动领取"),
    ENABLED(2, "自动领取");

    private final Integer code;
    private final String name;

    public static VoucherAutoClaimFlagEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> item.getCode().equals(code)).findFirst().orElse(null);
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
