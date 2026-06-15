package com.zy.common.core.enums.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 发放类型
 *
 * @author zzy
 * @date 2026/05/18
 */
@Getter
@AllArgsConstructor
public enum FulfillmentTypeEnum {
    CARD_SECRET(1, "卡密", RecipientAccountTypeEnum.NONE),
    WX_COUPON(2, "微信立减金", RecipientAccountTypeEnum.WX),
    ALIPAY_ACTIVITY(3, "支付宝红包", RecipientAccountTypeEnum.ALIPAY),
    MOBILE_RECHARGE(4, "手机号充值", RecipientAccountTypeEnum.MOBILE),
    MOBILE_PHONE_RECHARGE(5, "话费充值", RecipientAccountTypeEnum.MOBILE),
    ;

    /**
     * 发放类型
     */
    private final Integer type;

    /**
     * 发放类型描述
     */
    private final String desc;

    /**
     * 受益人账号类型
     */
    private final RecipientAccountTypeEnum accountTypeEnum;

    /**
     * 枚举映射
     */
    private static final Map<Integer, FulfillmentTypeEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(FulfillmentTypeEnum::getType, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据类型获取枚举
     *
     * @param type 类型
     * @return 枚举
     */
    public static FulfillmentTypeEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        return ENUM_MAP.get(type);
    }

    /**
     * 判断类型是否合法
     *
     * @param type 类型
     * @return 是否合法
     */
    public static boolean isValid(Integer type) {
        return getByType(type) != null;
    }
}
