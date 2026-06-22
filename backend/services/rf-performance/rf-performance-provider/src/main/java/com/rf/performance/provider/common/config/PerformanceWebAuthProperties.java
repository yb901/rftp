package com.rf.performance.provider.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 员工端 Web 登录配置。
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "rf.performance.web.auth")
public class PerformanceWebAuthProperties {

    /**
     * 员工端登录 Cookie 名称。
     */
    public static final String COOKIE_NAME = "webAuthToken";

    /**
     * 登录 Cookie 有效秒数。
     */
    private Integer cookieMaxAgeSeconds = 7 * 24 * 60 * 60;
}
