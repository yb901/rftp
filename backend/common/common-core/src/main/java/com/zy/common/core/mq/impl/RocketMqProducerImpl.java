package com.zy.common.core.mq.impl;

import com.zy.common.core.mq.RocketMqProducer;
import com.zy.common.core.trace.TraceConstants;
import com.zy.common.core.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * RocketMQ 消息发送器（Remoting 协议）
 * <p>
 * 支持发送普通消息和延时消息。
 */
@Slf4j
@Component(value = "rocketMqProducer")
@ConditionalOnProperty(prefix = "rocketmq.producer", name = "enable", havingValue = "true")
public class RocketMqProducerImpl implements RocketMqProducer, InitializingBean, DisposableBean {

    @Value("${spring.application.name}")
    private String appName;
    @Value("${rocketmq.name-server:127.0.0.1:9876}")
    private String nameServer;
    @Value("${rocketmq.producer.timeout:3000}")
    private Integer timeout;
    @Value("${zy.rocketmq.producer.retry-times:2}")
    private Integer retryTimes;

    private DefaultMQProducer producer;

    public void start() {
        try {
            producer = new DefaultMQProducer("pid-" + appName);
            producer.setNamesrvAddr(nameServer);
            producer.setSendMsgTimeout(timeout);
            producer.setRetryTimesWhenSendFailed(retryTimes);
            producer.start();
            log.info("RocketMQ producer started, group={}, nameServer={}", producer.getProducerGroup(), producer.getNamesrvAddr());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start RocketMQ producer", e);
        }
    }

    @EventListener(ApplicationStartedEvent.class)
    public void shutdown() {

    }

    /**
     * 发送普通消息
     *
     * @param topic 主题
     * @param tag   标签
     * @param body  消息体（字节数组）
     * @return 发送结果
     */
    @Override
    public boolean send(String topic, String tag, String body) {
        return send(topic, tag, body, new Message(topic, tag, body.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 发送普通消息
     *
     * @param topic 主题
     * @param tag   标签
     * @param body  消息体（字节数组）
     * @return 发送结果
     */
    private boolean send(String topic, String tag, String body, Message msg) {
        SendResult sendResult = null;
        try {
            msg.putUserProperty(TraceConstants.TRACE_ID, TraceContext.getOrCreateTraceId());
            sendResult = producer.send(msg);
            return true;
        } catch (Exception e) {
            log.error("RocketMQ producer send exception, topic={} tag={} body={}", topic, tag, body, e);
            return false;
        } finally {
            if (sendResult != null) {
                log.info("RocketMQ producer send result, topic={} tag={} msgId={} offsetMsgId={} body={}", topic, tag, sendResult.getMsgId(), sendResult.getOffsetMsgId(), body);
            }
        }
    }

    /**
     * 发送延时消息
     *
     * @param topic 主题
     * @param tag   标签
     * @param body  消息体（字节数组）
     * @return 发送结果
     */
    @Override
    public boolean sendDelayed(String topic, String tag, String body, TimeUnit timeUnit, long delayTime) {
        Message msg = new Message(topic, tag, body.getBytes(StandardCharsets.UTF_8));
        msg.setDeliverTimeMs(System.currentTimeMillis() + timeUnit.toMillis(delayTime));
        return send(topic, tag, body, msg);
    }

    @Override
    public void destroy() throws Exception {
        if (producer != null) {
            producer.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}
