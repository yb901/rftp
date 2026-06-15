package com.zy.common.core.enums.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调用供应商策略编码
 *
 * @author zzy
 * @date 2026/05/18
 */
@AllArgsConstructor
@Getter
public enum SupplierCallStrategyCodeEnum {

    WX_COUPON(1, "微信立减金", FulfillmentTypeEnum.WX_COUPON),
    ALIPAY_ACTIVITY(2, "支付宝活动", FulfillmentTypeEnum.ALIPAY_ACTIVITY),
    MOBILE_PHONE_RECHARGE_FU_LU(3, "话费充值-福禄", FulfillmentTypeEnum.MOBILE_PHONE_RECHARGE),
    ;
    /**
     * 调用供应商策略编码
     */
    private final Integer code;

    /**
     * 调用供应商策略编码描述
     */
    private final String desc;

    /**
     * 受益人信息模型类型
     */
    private final FulfillmentTypeEnum profileTypeEnum;


}
