package com.zy.common.core.mq;

import java.util.concurrent.TimeUnit;

/**
 * RocketMQ 消息发送器（Remoting 协议）
 * <p>
 * 支持发送普通消息和延时消息。
 */
public interface RocketMqProducer {

    /**
     * 发送普通消息
     *
     * @param topic 主题
     * @param tag   标签
     * @param body  消息体（字节数组）
     * @return 发送结果
     */
    boolean send(String topic, String tag, String body);

    /**
     * 发送延时消息
     *
     * @param topic 主题
     * @param tag   标签
     * @param body  消息体（字节数组）
     * @return 发送结果
     */
    boolean sendDelayed(String topic, String tag, String body, TimeUnit timeUnit, long delayTime);
}
