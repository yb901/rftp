package com.zy.common.core.dbencrypt.auto;

import com.alibaba.fastjson2.JSON;
import com.zy.common.core.dbencrypt.DbEncryptionPlugin;
import com.zy.common.core.dbencrypt.config.QyDbEncryptProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * MyBatis Bean后处理器
 * <p>
 * 用于在SqlSessionFactory创建后注入数据库加解密插件
 *
 * @author zzy
 * @date 2026/05/04
 */
public class QyMybatisBeanPostProcessor implements BeanPostProcessor {

    @Resource
    private QyDbEncryptProperties qyDbEncryptProperties;

    /**
     * Bean初始化前处理
     *
     * @param bean     Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean
     * @throws BeansException 如果发生异常
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Bean初始化后处理
     * <p>
     * 当检测到SqlSessionFactory或SqlSessionTemplate时，添加数据库加解密拦截器
     *
     * @param bean     Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean
     * @throws BeansException 如果发生异常
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        SqlSessionFactory s = null;
        if (bean instanceof SqlSessionFactory) {
            s = (SqlSessionFactory) bean;
        }
        if (bean instanceof SqlSessionTemplate) {
            s = ((SqlSessionTemplate) bean).getSqlSessionFactory();
        }
        if (s == null) {
            return bean;
        }

        addInterceptor(s);

        return bean;
    }

    /**
     * 添加加解密拦截器到SqlSessionFactory
     *
     * @param s SqlSessionFactory实例
     */
    private void addInterceptor(SqlSessionFactory s) {
        DbEncryptionPlugin plugin = new DbEncryptionPlugin();
        Properties properties = new Properties();
        properties.put("secretMap", JSON.toJSONString(qyDbEncryptProperties.getSecrets()));
        properties.put("tables", JSON.toJSONString(qyDbEncryptProperties.getTables()));
        properties.put("logLevel", qyDbEncryptProperties.getLogLevel());
        plugin.setProperties(properties);
        s.getConfiguration().addInterceptor(plugin);
    }
}
