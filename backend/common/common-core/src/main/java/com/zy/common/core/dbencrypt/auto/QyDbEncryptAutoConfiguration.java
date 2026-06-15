package com.zy.common.core.dbencrypt.auto;

import com.zy.common.core.dbencrypt.config.QyDbEncryptProperties;
import com.zy.common.core.dbencrypt.handler.command.impl.DeleteSqlHandler;
import com.zy.common.core.dbencrypt.handler.command.impl.InsertSqlHandler;
import com.zy.common.core.dbencrypt.handler.command.impl.SelectSqlHandler;
import com.zy.common.core.dbencrypt.handler.command.impl.UpdateSqlHandler;
import com.zy.common.core.dbencrypt.handler.encrypt.impl.Sm4Handler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 数据库加解密自动配置类
 *
 * @author zzy
 * @date 2026/05/04
 */
@AutoConfiguration
@EnableConfigurationProperties(QyDbEncryptProperties.class)
@ConditionalOnProperty(prefix = QyDbEncryptProperties.QUAN_YI_ENCRYPT_PREFIX, name = "enabled", havingValue = "true")
@Import({
        QyMybatisBeanPostProcessor.class,
        Sm4Handler.class,
        InsertSqlHandler.class,
        UpdateSqlHandler.class,
        DeleteSqlHandler.class,
        SelectSqlHandler.class
})
@ConditionalOnClass({SqlSessionTemplate.class, SqlSessionFactoryBean.class, SqlSessionFactory.class})
public class QyDbEncryptAutoConfiguration {
}
