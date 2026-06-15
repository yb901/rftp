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
 * RocketMQ 广播消费者
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "rocketmq.consumer.broadcast", name = "enable", havingValue = "true")
public class RocketMqBroadcastConsumer extends AbstractRocketMqConsumer {

    /**
     * 广播消费者订阅的 topic
     */
    @Value("${rocketmq.consumer.broadcast.topics:}")
    private String topics;


    @Override
    protected List<String> getTopics() {
        return Arrays.stream(topics.split(",")).filter(StringUtils::isNotEmpty).toList();
    }

    @Override
    protected String getConsumerSuffix() {
        return "broadcast";
    }

    @Override
    protected MessageModel getMessageModel() {
        return MessageModel.BROADCASTING;
    }

    @Override
    protected int getConsumeThreadMin() {
        return 8;
    }

    @Override
    protected int getConsumeThreadMax() {
        return 16;
    }
}
