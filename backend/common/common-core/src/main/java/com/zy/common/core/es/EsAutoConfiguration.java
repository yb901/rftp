package com.zy.common.core.es;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * ES 自动配置。
 *
 * <p>仅在业务服务显式引入 Spring Data Elasticsearch 并开启配置时注册公共 ES 工具。</p>
 *
 * @author zzy
 * @date 2026/05/20
 */
@AutoConfiguration(after = ElasticsearchDataAutoConfiguration.class)
@ConditionalOnClass(ElasticsearchOperations.class)
@ConditionalOnBean(ElasticsearchOperations.class)
@ConditionalOnProperty(prefix = "zy.elasticsearch", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(EsIndexSettingsOverrideProperties.class)
public class EsAutoConfiguration {

    /**
     * 注册 ES 搜索工具。
     *
     * @param elasticsearchOperations ES 操作模板
     * @return ES 搜索工具
     */
    @Bean
    @ConditionalOnMissingBean
    public EsSearchHelper esSearchHelper(ElasticsearchOperations elasticsearchOperations) {
        return new EsSearchHelper(elasticsearchOperations);
    }

    /**
     * 注册 ES 索引管理器。
     *
     * @param elasticsearchOperations           ES 操作模板
     * @param settingsOverrideProperties        索引 settings 覆盖配置
     * @param environment                       Spring 环境
     * @return ES 索引管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public EsIndexInitializer esIndexInitializer(ElasticsearchOperations elasticsearchOperations,
                                                EsIndexSettingsOverrideProperties settingsOverrideProperties,
                                                Environment environment) {
        return new EsIndexInitializer(elasticsearchOperations, settingsOverrideProperties, environment);
    }
}
