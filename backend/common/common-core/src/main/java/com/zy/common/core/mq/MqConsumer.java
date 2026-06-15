package com.zy.common.core.mq;

public interface MqConsumer {
    String getTopic();

    String getTag();

    void consume(String body);
}
