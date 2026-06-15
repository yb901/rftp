package com.zy.common.core.enums.delivery.voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 卡券账号采集方式
 *
 * @author zzy
 * @date 2026/05/18
 */
@Getter
@AllArgsConstructor
public enum VoucherAccountCollectModeEnum {
    NONE(1, "无需采集"),
    USER_INPUT(2, "用户输入"),
    WECHAT_AUTH(3, "微信授权获取"),
    ALIPAY_AUTH(4, "支付宝授权获取"),
    ;

    /**
     * 卡券账号采集方式
     */
    private final Integer code;

    /**
     * 卡券账号采集方式描述
     */
    private final String desc;
}
