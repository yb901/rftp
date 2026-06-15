package com.zy.common.core.order;

import org.apache.commons.lang3.StringUtils;

/**
 * 订单号分片解析器
 *
 * <p>根据任意订单号提取分片键、订单类型与环境信息。</p>
 *
 * @author zzy
 * @date 2026/06/01
 */
public final class OrderNoShardParser {

    /**
     * 私有构造方法
     */
    private OrderNoShardParser() {
    }

    /**
     * 订单最小长度（首位 + 三位分片）
     */
    private static final int MIN_ORDER_NO_LENGTH = 4;

    /**
     * 根据任意订单号提取分片键（第 2-4 位）
     *
     * @param orderNo 订单号
     * @return 分片键
     */
    public static String parseShardKey(String orderNo) {
        if (!isValidOrderNo(orderNo)) {
            return null;
        }
        return orderNo.substring(1, 4);
    }

    /**
     * 根据订单号识别订单类型
     *
     * @param orderNo 订单号
     * @return 订单类型
     */
    public static OrderTypeEnum parseOrderType(String orderNo) {
        if (!isValidOrderNo(orderNo)) {
            return null;
        }
        return OrderTypeEnum.fromCode(orderNo.charAt(0));
    }

    /**
     * 根据订单号识别订单环境
     *
     * @param orderNo 订单号
     * @return 订单环境
     */
    public static OrderEnvironmentEnum parseEnvironment(String orderNo) {
        if (!isValidOrderNo(orderNo)) {
            return null;
        }
        return OrderTypeEnum.resolveEnvironment(orderNo.charAt(0));
    }

    /**
     * 校验订单号最小格式
     *
     * @param orderNo 订单号
     * @return 校验结果
     */
    private static boolean isValidOrderNo(String orderNo) {
        return StringUtils.isNotBlank(orderNo) && orderNo.length() >= MIN_ORDER_NO_LENGTH;
    }
}
