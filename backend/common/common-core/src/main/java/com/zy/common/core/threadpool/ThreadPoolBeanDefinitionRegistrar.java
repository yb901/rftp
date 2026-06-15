package com.zy.common.core.threadpool;

import com.zy.common.core.trace.thread.TraceThreadPoolExecutor;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用线程池 Bean 定义注册器
 *
 * @author zzy
 * @date 2026/05/18
 */
public class ThreadPoolBeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    /**
     * Spring 环境变量
     */
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Map<String, ThreadPoolProperties.Pool> pools = Binder.get(environment)
                .bind(ThreadPoolProperties.PREFIX, Bindable.mapOf(String.class, ThreadPoolProperties.Pool.class))
                .orElseGet(Map::of);
        if (CollectionUtils.isEmpty(pools)) {
            return;
        }
        for (Map.Entry<String, ThreadPoolProperties.Pool> entry : pools.entrySet()) {
            registerExecutorService(registry, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * 注册线程池 Bean
     *
     * @param registry  Bean 注册器
     * @param poolName  线程池配置名称
     * @param pool      线程池配置
     */
    private void registerExecutorService(BeanDefinitionRegistry registry, String poolName, ThreadPoolProperties.Pool pool) {
        if (pool == null || !Boolean.TRUE.equals(pool.getEnabled())) {
            return;
        }

        // 校验线程池配置
        validatePool(poolName, pool);

        // 生成线程池 Bean 名称
        String beanName = toExecutorServiceBeanName(poolName);
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }

        // 计算线程池核心参数
        int corePoolSize = pool.getCoreSize();
        int maximumPoolSize = Math.max(pool.getCoreSize(), pool.getMaxSize());
        long keepAliveTime = pool.getKeepAliveSeconds();

        // 构建线程池阻塞队列
        BlockingQueue<Runnable> workQueue = buildWorkQueue(pool.getQueueSize());

        // 构建线程池 Bean 定义
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(TraceThreadPoolExecutor.class)
                .addConstructorArgValue(corePoolSize)
                .addConstructorArgValue(maximumPoolSize)
                .addConstructorArgValue(keepAliveTime)
                .addConstructorArgValue(TimeUnit.SECONDS)
                .addConstructorArgValue(workQueue)
                .addConstructorArgValue(new ThreadPoolNamedThreadFactory(beanName))
                .addConstructorArgValue(new ThreadPoolRejectedExecutionHandler(beanName))
                .setDestroyMethodName("shutdown")
                .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 构建阻塞队列
     *
     * @param queueSize 队列大小
     * @return 阻塞队列
     */
    private BlockingQueue<Runnable> buildWorkQueue(Integer queueSize) {
        if (queueSize != null && queueSize > 0) {
            return new LinkedBlockingQueue<>(queueSize);
        }
        return new SynchronousQueue<>();
    }

    /**
     * 校验线程池配置
     *
     * @param poolName 线程池配置名称
     * @param pool     线程池配置
     */
    private void validatePool(String poolName, ThreadPoolProperties.Pool pool) {
        if (!StringUtils.hasText(poolName)) {
            throw new IllegalArgumentException("Thread pool name must not be blank");
        }
        if (pool.getCoreSize() == null || pool.getCoreSize() <= 0) {
            throw new IllegalArgumentException("Thread pool core-size must be greater than 0: " + poolName);
        }
        if (pool.getMaxSize() == null || pool.getMaxSize() <= 0) {
            throw new IllegalArgumentException("Thread pool max-size must be greater than 0: " + poolName);
        }
        if (pool.getQueueSize() == null || pool.getQueueSize() < 0) {
            throw new IllegalArgumentException("Thread pool queue-size must be greater than or equal to 0: " + poolName);
        }
        if (pool.getKeepAliveSeconds() == null || pool.getKeepAliveSeconds() < 0) {
            throw new IllegalArgumentException("Thread pool keep-alive-seconds must be greater than or equal to 0: " + poolName);
        }
    }

    /**
     * 转换线程池 Bean 名称
     *
     * @param poolName 线程池配置名称
     * @return 线程池 Bean 名称
     */
    public static String toExecutorServiceBeanName(String poolName) {
        if (!StringUtils.hasText(poolName)) {
            throw new IllegalArgumentException("Thread pool name must not be blank");
        }
        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < poolName.length(); i++) {
            char ch = poolName.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                builder.append(upperNext ? Character.toUpperCase(ch) : ch);
                upperNext = false;
            } else {
                upperNext = true;
            }
        }
        String normalized = builder.toString();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("Thread pool name must contain letters or digits: " + poolName);
        }
        return Introspector.decapitalize(normalized) + ExecutorService.class.getSimpleName();
    }
}
