package com.zy.common.core.oss;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class AliOssAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AliOssAutoConfiguration.class));

    @Test
    void shouldNotCreateBeanWhenOssSdkIsAbsent() {
        contextRunner
                .withPropertyValues(
                        "aliyun.oss.endpoints.primary.url=https://oss-cn-hangzhou.aliyuncs.com",
                        "aliyun.oss.endpoints.primary.access-key-id=ak",
                        "aliyun.oss.endpoints.primary.access-key-secret=sk",
                        "aliyun.oss.endpoints.primary.bucket=bucket")
                .run(context -> assertThat(context).doesNotHaveBean(AliOssService.class));
    }

    @Test
    void shouldBindMultipleEndpointProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("aliyun.oss.enabled", "false")
                .withProperty("aliyun.oss.endpoints.primary.url", "https://oss-cn-hangzhou.aliyuncs.com")
                .withProperty("aliyun.oss.endpoints.primary.access-key-id", "primary-ak")
                .withProperty("aliyun.oss.endpoints.primary.access-key-secret", "primary-sk")
                .withProperty("aliyun.oss.endpoints.primary.bucket", "primary-bucket")
                .withProperty("aliyun.oss.endpoints.backup.url", "https://oss-cn-shanghai.aliyuncs.com")
                .withProperty("aliyun.oss.endpoints.backup.access-key-id", "backup-ak")
                .withProperty("aliyun.oss.endpoints.backup.access-key-secret", "backup-sk")
                .withProperty("aliyun.oss.endpoints.backup.bucket", "backup-bucket");

        AliOssProperties properties = Binder.get(environment)
                .bind(AliOssProperties.PREFIX, Bindable.of(AliOssProperties.class))
                .orElseGet(AliOssProperties::new);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getEndpoints()).containsOnlyKeys("primary", "backup");
        assertThat(properties.getEndpoints().get("primary").getBucket()).isEqualTo("primary-bucket");
        assertThat(properties.getEndpoints().get("backup").getAccessKeyId()).isEqualTo("backup-ak");
    }

    @Test
    void shouldGenerateAliOssServiceBeanNameFromEndpointName() {
        assertThat(AliOssBeanDefinitionRegistrar.toAliOssServiceBeanName("primary")).isEqualTo("primaryAliOssService");
        assertThat(AliOssBeanDefinitionRegistrar.toAliOssServiceBeanName("backup-store")).isEqualTo("backupStoreAliOssService");
    }
}
