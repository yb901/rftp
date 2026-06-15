package com.zy.common.core.enums.delivery.voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡券有效期类型枚举
 *
 * @author zzy
 * @date 2026/06/05
 */
@Getter
@AllArgsConstructor
public enum VoucherValidityTypeEnum {

    ABSOLUTE_TIME(1, "截止到"),
    AFTER_CREATE_DAYS(2, "卡券生成后N天"),
    CURRENT_MONTH_END(3, "卡券生成当月月底"),
    CURRENT_WEEK_SUNDAY(4, "卡券生成当周周日");

    private final Integer code;
    private final String name;

    public static VoucherValidityTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> item.getCode().equals(code)).findFirst().orElse(null);
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
