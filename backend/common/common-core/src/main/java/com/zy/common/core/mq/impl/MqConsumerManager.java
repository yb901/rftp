package com.zy.common.core.mq.impl;

import com.zy.common.core.mq.MqConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MqConsumerManager {

    private static Map<String, Map<String, MqConsumer>> mqConsumerMap = new HashMap<>();

    public static void register(MqConsumer consumer) {
        Map<String, MqConsumer> tagMqConsumerMap = mqConsumerMap.computeIfAbsent(consumer.getTopic(), k -> new HashMap<>());
        tagMqConsumerMap.put(consumer.getTag(), consumer);
    }

    public static MqConsumer get(String topic, String tag) {
        Map<String, MqConsumer> tagMqConsumerMap = mqConsumerMap.get(topic);
        if (tagMqConsumerMap == null) {
            return null;
        }
        return tagMqConsumerMap.get(tag);
    }
}
