package com.zy.common.core.mq.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * RocketMQ 消息消费者工厂（Remoting 协议）
 * <p>
 * 支持集群消费和广播消费。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "rocketmq.consumer", name = "enable", havingValue = "true")
public class RocketMqConsumer extends AbstractRocketMqConsumer {

    /**
     * 订阅的 topic，支持多个，用逗号分隔
     */
    @Value("${rocketmq.consumer.topics:}")
    private String topics;

    @Override
    protected List<String> getTopics() {
        return Arrays.stream(topics.split(",")).filter(StringUtils::isNotEmpty).toList();
    }

    @Override
    protected String getConsumerSuffix() {
        return "";
    }

    @Override
    protected MessageModel getMessageModel() {
        return MessageModel.CLUSTERING;
    }

    @Override
    protected int getConsumeThreadMin() {
        return 16;
    }

    @Override
    protected int getConsumeThreadMax() {
        return 32;
    }
}
