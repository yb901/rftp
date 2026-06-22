package com.rf.mng.provider.common.config.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 平台主库数据源配置。
 */
@Configuration
@MapperScan(
        basePackages = "com.rf.mng.provider.infrastructure.persistence.platform",
        sqlSessionFactoryRef = "platformSqlSessionFactory",
        sqlSessionTemplateRef = "platformSqlSessionTemplate"
)
public class PlatformDataSourceConfig {

    /** 平台主库配置属性。 */
    @Primary
    @Bean("platformDataSourceProperties")
    @ConfigurationProperties("spring.datasource.platform")
    public DataSourceProperties platformDataSourceProperties() {
        return new DataSourceProperties();
    }

    /** 平台主库数据源。 */
    @Primary
    @Bean("dataSource")
    public DataSource platformDataSource(@Qualifier("platformDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /** 平台主库事务管理器。 */
    @Primary
    @Bean("transactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /** 平台主库 MyBatis 会话工厂。 */
    @Primary
    @Bean("platformSqlSessionFactory")
    public SqlSessionFactory platformSqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/platform/**/*.xml"));
        factoryBean.setConfiguration(mybatisConfiguration());
        return factoryBean.getObject();
    }

    /** 平台主库 MyBatis 会话模板。 */
    @Primary
    @Bean("platformSqlSessionTemplate")
    public SqlSessionTemplate platformSqlSessionTemplate(
            @Qualifier("platformSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    private org.apache.ibatis.session.Configuration mybatisConfiguration() {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }
}
