package com.rfpt.performance.provider.common.config.job;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.zy.common.core.trace.xxljob.TraceXxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * XXL-JOB 执行器配置类。
 */
@Slf4j
@Configuration
public class XxlJobConfig {

    /**
     * XXL-JOB 配置属性。
     */
    @Resource
    private XxlJobProperties xxlJobProperties;

    /**
     * 创建 XXL-JOB Spring 执行器。
     *
     * @return XXL-JOB Spring 执行器
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new TraceXxlJobSpringExecutor();
        executor.setAdminAddresses(xxlJobProperties.getAdmin().getAddresses());
        executor.setAppname(xxlJobProperties.getExecutor().getAppname());
        executor.setAddress(xxlJobProperties.getExecutor().getAddress());
        executor.setIp(xxlJobProperties.getExecutor().getIp());
        executor.setPort(xxlJobProperties.getExecutor().getPort());
        executor.setAccessToken(xxlJobProperties.getAccessToken());
        executor.setLogPath(xxlJobProperties.getExecutor().getLogpath());
        executor.setLogRetentionDays(xxlJobProperties.getExecutor().getLogretentiondays());
        log.info("XXL-JOB executor initialized, appname={}, admin={}",
                xxlJobProperties.getExecutor().getAppname(), xxlJobProperties.getAdmin().getAddresses());
        return executor;
    }
}
