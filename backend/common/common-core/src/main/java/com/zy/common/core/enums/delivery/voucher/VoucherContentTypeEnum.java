package com.zy.common.core.enums.delivery.voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡券内容形式枚举
 *
 * @author zzy
 * @date 2026/06/05
 */
@Getter
@AllArgsConstructor
public enum VoucherContentTypeEnum {

    LINK(1, "链接券"),
    FIXED_LINK_CODE(2, "固定链接+券码");

    private final Integer code;
    private final String name;

    public static VoucherContentTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> item.getCode().equals(code)).findFirst().orElse(null);
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
