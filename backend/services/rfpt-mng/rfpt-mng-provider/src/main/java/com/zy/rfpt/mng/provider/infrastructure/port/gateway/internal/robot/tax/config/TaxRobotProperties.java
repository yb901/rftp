package com.zy.rfpt.mng.provider.infrastructure.port.gateway.internal.robot.tax.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 税务机器人配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rfpt.robot.tax")
public class TaxRobotProperties {

    /** 机器人服务地址。 */
    private String baseUrl;

    /** 内部调用令牌。 */
    private String internalToken;

    /** 请求超时时间。 */
    private Long timeoutMillis = 330000L;
}
