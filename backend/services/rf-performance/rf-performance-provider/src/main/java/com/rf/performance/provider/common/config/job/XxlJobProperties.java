package com.rf.performance.provider.common.config.job;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * XXL-JOB 执行器配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    /**
     * 调度中心地址配置。
     */
    private Admin admin = new Admin();

    /**
     * 执行器配置。
     */
    private Executor executor = new Executor();

    /**
     * 访问令牌。
     */
    private String accessToken;

    /**
     * XXL-JOB 调度中心配置。
     */
    @Data
    public static class Admin {

        /**
         * 调度中心地址，多个地址用逗号分隔。
         */
        private String addresses;
    }

    /**
     * XXL-JOB 执行器配置。
     */
    @Data
    public static class Executor {

        /**
         * 执行器应用名。
         */
        private String appname = "rf-performance";

        /**
         * 执行器注册地址。
         */
        private String address;

        /**
         * 执行器 IP。
         */
        private String ip;

        /**
         * 执行器端口。
         */
        private Integer port = 9996;

        /**
         * 执行日志目录。
         */
        private String logpath = "./logs/xxl-job";

        /**
         * 执行日志保留天数。
         */
        private Integer logretentiondays = 30;
    }
}
