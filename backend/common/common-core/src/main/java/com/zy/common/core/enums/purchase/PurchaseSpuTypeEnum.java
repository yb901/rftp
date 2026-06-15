package com.zy.common.core.enums.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目商品类型枚举
 *
 * @author zzy
 * @date 2026/05/19
 */
@Getter
@AllArgsConstructor
public enum PurchaseSpuTypeEnum {

    /**
     * 直充
     */
    DIRECT_RECHARGE(1, "直充"),

    /**
     * 快捷支付
     */
    QUICK_PAY(2, "快捷支付"),

    /**
     * 垫资商品
     */
    ADVANCE_PAYMENT(3, "垫资商品"),

    /**
     * 卡密
     */
    CARD_SECRET(4, "卡密");

    /**
     * 项目商品类型编码
     */
    private final Integer code;

    /**
     * 项目商品类型名称
     */
    private final String name;

    /**
     * 枚举映射
     */
    private static final Map<Integer, PurchaseSpuTypeEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(PurchaseSpuTypeEnum::getCode, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据项目商品类型编码获取枚举
     *
     * @param code 项目商品类型编码
     * @return 项目商品类型枚举
     */
    public static PurchaseSpuTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return ENUM_MAP.get(code);
    }

    /**
     * 判断商品类型编码是否匹配项目商品类型
     *
     * @param goodsType 商品类型编码
     * @return 是否匹配
     */
    public boolean matchesPurchaseSpuType(Integer goodsType) {
        return this.code.equals(goodsType);
    }
}
