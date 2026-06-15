package com.zy.common.core.threadpool;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 通用线程池自动配置
 *
 * @author zzy
 * @date 2026/05/18
 */
@AutoConfiguration
public class ThreadPoolAutoConfiguration {

    /**
     * 注册线程池 Bean 定义处理器
     *
     * @return 线程池 Bean 定义处理器
     */
    @Bean
    public static ThreadPoolBeanDefinitionRegistrar threadPoolBeanDefinitionRegistrar() {
        return new ThreadPoolBeanDefinitionRegistrar();
    }
}
