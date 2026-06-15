package com.zy.common.core.order;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成器
 *
 * <p>按规则组合：订单类型首位 + 三位分片键 + 时间戳 + 发号后缀。</p>
 *
 * @author zzy
 * @date 2026/06/01
 */
public final class OrderNoGenerator {

    /**
     * 私有构造方法
     */
    private OrderNoGenerator() {
    }

    /**
     * 上海时区
     */
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 时间戳格式
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 生成订单号
     *
     * @param orderType    订单类型
     * @param shardKey     三位分片键
     * @param environment  订单环境
     * @param sequencePart 发号后缀
     * @return 订单号
     */
    public static String generate(OrderTypeEnum orderType,
                                  String shardKey,
                                  OrderEnvironmentEnum environment,
                                  String sequencePart) {
        if (orderType == null) {
            throw new IllegalArgumentException("订单类型不能为空");
        }
        if (!ShardSuffixResolver.isValidShardKey(shardKey)) {
            throw new IllegalArgumentException("分片键格式不合法");
        }
        if (environment == null) {
            throw new IllegalArgumentException("订单环境不能为空");
        }
        if (StringUtils.isBlank(sequencePart)) {
            throw new IllegalArgumentException("发号后缀不能为空");
        }

        // 生成当前毫秒时间戳段
        String timestamp = LocalDateTime.now(SHANGHAI_ZONE).format(TIMESTAMP_FORMATTER);

        // 组合订单号
        return new StringBuilder()
                .append(orderType.firstCode(environment))
                .append(shardKey)
                .append(timestamp)
                .append(sequencePart)
                .toString();
    }
}
