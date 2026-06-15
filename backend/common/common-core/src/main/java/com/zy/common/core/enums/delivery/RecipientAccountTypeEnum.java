package com.zy.common.core.enums.delivery;

import com.zy.common.core.enums.delivery.voucher.VoucherAccountCollectModeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 受益人账号类型
 *
 * @author zzy
 * @date 2026/05/18
 */
@Getter
@AllArgsConstructor
public enum RecipientAccountTypeEnum {
    NONE(1, "无需账号", VoucherAccountCollectModeEnum.NONE),
    WX(2, "微信", VoucherAccountCollectModeEnum.WECHAT_AUTH),
    ALIPAY(3, "支付宝", VoucherAccountCollectModeEnum.ALIPAY_AUTH),
    MOBILE(4, "手机号", VoucherAccountCollectModeEnum.USER_INPUT),
    ;

    /**
     * 受益人账号类型
     */
    private final Integer type;

    /**
     * 受益人账号类型描述
     */
    private final String desc;

    /**
     * 卡券账号采集方式
     */
    private final VoucherAccountCollectModeEnum collectModeEnum;

    /**
     * 枚举映射
     */
    private static final Map<Integer, RecipientAccountTypeEnum> ENUM_MAP = Collections.unmodifiableMap(
            Arrays.stream(values()).collect(Collectors.toMap(RecipientAccountTypeEnum::getType, Function.identity(), (left, right) -> right))
    );

    /**
     * 根据类型获取枚举
     *
     * @param type 类型
     * @return 枚举
     */
    public static RecipientAccountTypeEnum getByType(Integer type) {
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
