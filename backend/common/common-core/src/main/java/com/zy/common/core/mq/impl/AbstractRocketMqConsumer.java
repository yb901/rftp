package com.zy.common.core.mq.impl;

import com.zy.common.core.mq.MqConsumer;
import com.zy.common.core.trace.TraceConstants;
import com.zy.common.core.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.deploy.ApplicationDeployListener;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.event.EventListener;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RocketMQ 消息消费者工厂（Remoting 协议）
 * <p>
 * 支持集群消费和广播消费。
 */
@Slf4j
public abstract class AbstractRocketMqConsumer implements ApplicationDeployListener {

    @Value("${spring.application.name}")
    private String appName;
    @Value("${rocketmq.name-server:127.0.0.1:9876}")
    private String nameServer;
    private DefaultMQPushConsumer consumer;

    @EventListener(ApplicationReadyEvent.class)
    public void run(SpringApplicationEvent event) {
        String suffix = getConsumerSuffix();
        String consumerGroup = StringUtils.isNotEmpty(suffix) ? "cid-" + appName + "-" + suffix : "cid-" + appName;
        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);
        consumer.setMessageModel(getMessageModel());
        consumer.setConsumeThreadMin(getConsumeThreadMin());
        consumer.setConsumeThreadMax(getConsumeThreadMax());

        try {
            List<String> topics = getTopics();
            log.info("RocketMQ consumer subscribing, group={}, topics={}", consumerGroup, topics);
            for (String topic : topics) {
                consumer.subscribe(topic, "*");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RocketMQ consumer", e);
        }
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                String previousTraceId = TraceContext.getTraceId();
                String traceId = TraceContext.normalizeOrCreate(msg.getUserProperty(TraceConstants.TRACE_ID));
                TraceContext.setTraceId(traceId);
                try {
                    String topic = msg.getTopic();
                    String tag = msg.getTags();
                    String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                    log.info("RocketMQ consume, topic={}, tag={}, body={}", topic, tag, body);
                    MqConsumer consumer = MqConsumerManager.get(topic, tag);
                    if (consumer != null) {
                        consumer.consume(body);
                    } else {
                        log.info("cannot consume topic={} tag={}", topic, tag);
                    }
                } catch (Exception e) {
                    log.error("RocketMQ consume failed, topic={}, msgId={}, reconsumeTimes={}", msg.getTopic(), msg.getMsgId(), msg.getReconsumeTimes(), e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                } finally {
                    TraceContext.restore(previousTraceId);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        try {
            consumer.start();
            log.info("RocketMQ consumer started, group={}, nameServer={}", consumer.getConsumerGroup(), consumer.getNamesrvAddr());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start RocketMQ consumer", e);
        }
    }

    @Override
    public void onFailure(ApplicationModel scopeModel, Throwable cause) {

    }

    @Override
    public void onInitialize(ApplicationModel scopeModel) {

    }

    @Override
    public void onStopping(ApplicationModel scopeModel) {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    @Override
    public void onStarted(ApplicationModel scopeModel) {
    }

    @Override
    public void onStarting(ApplicationModel scopeModel) {
    }

    @Override
    public void onStopped(ApplicationModel scopeModel) {
    }


    protected abstract List<String> getTopics();

    protected abstract String getConsumerSuffix();

    protected abstract MessageModel getMessageModel();

    protected abstract int getConsumeThreadMin();

    protected abstract int getConsumeThreadMax();
}
