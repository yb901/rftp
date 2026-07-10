package com.rf.performance.provider.common.config;

import com.zy.common.core.datasource.QyHikariDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 员工绩效服务数据源配置。
 */
@Configuration
public class PerformanceDataSourceConfig {

    /** 平台主库数据源。 */
    @Primary
    @Bean(name = "dataSource", destroyMethod = "close")
    @ConfigurationProperties(prefix = "datasource.rf-pt")
    public HikariDataSource dataSource(QyHikariDataSourceFactory dataSourceFactory) {
        return dataSourceFactory.create("rf-pt");
    }

    /** 平台主库事务管理器。 */
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
