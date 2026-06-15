package com.zy.common.core.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

@AutoConfiguration  // 避免 CGLIB 代理增强，减少类加载时机
@ConditionalOnClass({RedisLockRegistry.class, RedisConnectionFactory.class})  // 只有类存在时才解析此配置类
@ConditionalOnProperty(name = "redis.lock.enabled", havingValue = "true")
public class RedisLockConfig {

    // 从配置中获取应用名称，若未配置则默认为 "unknown"
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        String lockPrefix = "redis-lock_" + applicationName;
        // 过期时间也可以配置化，这里保持 20000 ms
        return new RedisLockRegistry(redisConnectionFactory, lockPrefix, 20000);
    }
}
