package com.zy.common.core.mq;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class MqTopicConfig {

    @Value("${rocketmq.topic.qy-mng:TopicQyMngTest}")
    private String topicQyMng;

    @Value("${rocketmq.topic.zy-api-web:TopicApiTest}")
    private String topicApi;

    @Value("${rocketmq.topic.zy-api-web:TopicDelayApiTest}")
    private String topicDelayApi;

    @Value("${rocketmq.topic.broadcast.zy-api-web:TopicBroadcastApiTest}")
    private String topicBroadcastApi;

    @Value("${rocketmq.topic.broadcast.qy-integration:TopicBroadcastQyIntegrationTest}")
    private String topicBroadcastQyIntegration;

    @Value("${rocketmq.topic.qy-integration:TopicQyIntegrationTest}")
    private String topicIntegration;

    @Value("${rocketmq.topic.qy-delivery:TopicQyDeliveryTest}")
    private String topicDelivery;

    @Value("${rocketmq.topic.broadcast.qy-delivery:TopicBroadcastQyDeliveryTest}")
    private String topicBroadcastQyDelivery;
}
