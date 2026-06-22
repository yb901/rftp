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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 机器人协作库数据源配置。
 */
@Configuration
@MapperScan(
        basePackages = "com.rf.mng.provider.infrastructure.persistence.robot",
        sqlSessionFactoryRef = "robotSqlSessionFactory",
        sqlSessionTemplateRef = "robotSqlSessionTemplate"
)
public class RobotDataSourceConfig {

    /** 机器人协作库配置属性。 */
    @Bean("robotDataSourceProperties")
    @ConfigurationProperties("spring.datasource.robot")
    public DataSourceProperties robotDataSourceProperties() {
        return new DataSourceProperties();
    }

    /** 机器人协作库数据源。 */
    @Bean("robotDataSource")
    public DataSource robotDataSource(@Qualifier("robotDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /** 机器人协作库事务管理器。 */
    @Bean("robotTransactionManager")
    public DataSourceTransactionManager robotTransactionManager(@Qualifier("robotDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /** 机器人协作库 MyBatis 会话工厂。 */
    @Bean("robotSqlSessionFactory")
    public SqlSessionFactory robotSqlSessionFactory(@Qualifier("robotDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/robot/**/*.xml"));
        factoryBean.setConfiguration(mybatisConfiguration());
        return factoryBean.getObject();
    }

    /** 机器人协作库 MyBatis 会话模板。 */
    @Bean("robotSqlSessionTemplate")
    public SqlSessionTemplate robotSqlSessionTemplate(
            @Qualifier("robotSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    private org.apache.ibatis.session.Configuration mybatisConfiguration() {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }
}
