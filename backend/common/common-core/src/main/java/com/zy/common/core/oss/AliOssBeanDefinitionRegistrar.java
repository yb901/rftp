package com.zy.common.core.oss;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.util.Map;

public class AliOssBeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        AliOssProperties properties = Binder.get(environment)
                .bind(AliOssProperties.PREFIX, Bindable.of(AliOssProperties.class))
                .orElseGet(AliOssProperties::new);
        if (!properties.isEnabled()) {
            return;
        }
        if (CollectionUtils.isEmpty(properties.getEndpoints())) {
            throw new AliOssException("OSS endpoints must not be empty when aliyun.oss is enabled");
        }
        for (Map.Entry<String, AliOssProperties.Endpoint> entry : properties.getEndpoints().entrySet()) {
            String endpointName = entry.getKey();
            String beanName = toAliOssServiceBeanName(endpointName);
            if (registry.containsBeanDefinition(beanName)) {
                continue;
            }
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(AliOssServiceImpl.class)
                    .addConstructorArgValue(endpointName)
                    .addConstructorArgValue(entry.getValue())
                    .setDestroyMethodName("shutdown")
                    .getBeanDefinition();
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    public static String toAliOssServiceBeanName(String endpointName) {
        if (!StringUtils.hasText(endpointName)) {
            throw new AliOssException("OSS endpoint name must not be blank");
        }
        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < endpointName.length(); i++) {
            char ch = endpointName.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                builder.append(upperNext ? Character.toUpperCase(ch) : ch);
                upperNext = false;
            } else {
                upperNext = true;
            }
        }
        String normalized = builder.toString();
        if (!StringUtils.hasText(normalized)) {
            throw new AliOssException("OSS endpoint name must contain letters or digits: " + endpointName);
        }
        return Introspector.decapitalize(normalized) + "AliOssService";
    }
}
