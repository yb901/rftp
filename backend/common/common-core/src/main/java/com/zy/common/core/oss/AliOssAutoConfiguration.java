package com.zy.common.core.oss;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@AutoConfiguration
@ConditionalOnClass(name = "com.aliyun.oss.OSS")
@ConditionalOnProperty(prefix = AliOssProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({AliOssProperties.class})
public class AliOssAutoConfiguration {

    @Bean
    public static AliOssBeanDefinitionRegistrar aliOssBeanDefinitionRegistrar() {
        return new AliOssBeanDefinitionRegistrar();
    }

    @Bean
    @ConditionalOnMissingBean
    public AliOssManager aliOssManager(Map<String, AliOssService> services) {
        if (CollectionUtils.isEmpty(services)) {
            throw new AliOssException("OSS service beans must not be empty when aliyun.oss is enabled");
        }
        return new AliOssManagerImpl(services);
    }
}
