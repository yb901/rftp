package com.zy.common.core.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 订单类型枚举
 *
 * @author zzy
 * @date 2026/06/01
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    /**
     * 交付订单号
     */
    DELIVERY_ORDER('3', 'c', "交付订单号"),

    /**
     * 交付项订单号
     */
    DELIVERY_ORDER_ITEM('4', 'd', "交付项订单号"),

    /**
     * 供应商尝试单号
     */
    SUPPLIER_ATTEMPT('5', 'e', "供应商尝试单号"),

    /**
     * 供应商订单号
     */
    SUPPLIER_ORDER('6', 'f', "供应商订单号"),

    /**
     * 交付库存扣减任务号
     */
    DELIVERY_STOCK_DEDUCT_TASK('7', 'g', "交付库存扣减任务号"),

    /**
     * 交付库存释放任务号
     */
    DELIVERY_STOCK_RELEASE_TASK('8', 'h', "交付库存释放任务号");

    /**
     * 生产环境首位编码
     */
    private final char prodCode;

    /**
     * 测试环境首位编码
     */
    private final char testCode;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据首位编码识别订单类型
     *
     * @param code 首位编码
     * @return 订单类型
     */
    public static OrderTypeEnum fromCode(char code) {
        return Arrays.stream(values())
                .filter(item -> item.prodCode == code || item.testCode == code)
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据首位编码识别环境
     *
     * @param code 首位编码
     * @return 订单环境
     */
    public static OrderEnvironmentEnum resolveEnvironment(char code) {
        for (OrderTypeEnum item : values()) {
            if (item.prodCode == code) {
                return OrderEnvironmentEnum.PROD;
            }
            if (item.testCode == code) {
                return OrderEnvironmentEnum.TEST;
            }
        }
        return null;
    }

    /**
     * 读取指定环境下的首位编码
     *
     * @param environment 订单环境
     * @return 首位编码
     */
    public char firstCode(OrderEnvironmentEnum environment) {
        if (environment == OrderEnvironmentEnum.TEST) {
            return testCode;
        }
        return prodCode;
    }
}
