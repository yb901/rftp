package com.zy.common.core.idgen.conf;

import com.zy.common.core.idgen.IdGeneratorClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ID 生成器自动配置类
 *
 * <p>当 classpath 中存在 Redis 操作类时自动启用。
 * 会扫描 {@code idgen.scenes} 配置并创建 {@link IdGeneratorClient} Bean。
 *
 * @author zzy
 * @date 2026-05-05
 * @see IdGeneratorClient
 * @see IdGeneratorProperties
 */
@EnableScheduling
@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(IdGeneratorProperties.class)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnBean(StringRedisTemplate.class)
public class IdGeneratorConfiguration {

    @Bean
    @Primary
    public IdGeneratorClient idGeneratorClient(StringRedisTemplate stringRedisTemplate) {
        return new IdGeneratorClient(stringRedisTemplate);
    }
}
