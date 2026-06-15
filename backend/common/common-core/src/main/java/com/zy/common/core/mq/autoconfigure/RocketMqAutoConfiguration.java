package com.zy.common.core.mq.autoconfigure;

import com.zy.common.core.mq.MqTopicConfig;
import com.zy.common.core.mq.impl.RocketMqBroadcastConsumer;
import com.zy.common.core.mq.impl.RocketMqConsumer;
import com.zy.common.core.mq.impl.RocketMqProducerImpl;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * RocketMQ 自动配置。
 *
 * <p>通过导入现有的 topic 配置、生产者和消费者 Bean，避免依赖大范围组件扫描。
 */
@AutoConfiguration
@ConditionalOnClass({DefaultMQProducer.class, DefaultMQPushConsumer.class})
@Import({
        MqTopicConfig.class,
        RocketMqProducerImpl.class,
        RocketMqConsumer.class,
        RocketMqBroadcastConsumer.class
})
public class RocketMqAutoConfiguration {
}
