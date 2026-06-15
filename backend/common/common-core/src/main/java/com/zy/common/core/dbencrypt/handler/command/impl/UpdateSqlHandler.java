package com.zy.common.core.dbencrypt.handler.command.impl;

import com.zy.common.core.dbencrypt.handler.command.SqlCommandAdapter;
import com.zy.common.core.dbencrypt.handler.command.SqlCommandHandler;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * UPDATE SQL命令处理器
 * <p>
 * 负责解析UPDATE语句，提取表名和需要加密的参数索引
 *
 * @author zzy
 * @date 2026/05/04
 */
@Configuration
@ConditionalOnClass({SqlSessionTemplate.class, SqlSessionFactoryBean.class, SqlSessionFactory.class})
public class UpdateSqlHandler extends AbstractSqlCommandHandler implements SqlCommandHandler {

    /**
     * 获取SQL命令类型
     *
     * @return UPDATE命令类型
     */
    @Override
    public SqlCommandType sqlCommandType() {
        return SqlCommandType.UPDATE;
    }

    /**
     * 从UPDATE SQL中提取表名
     *
     * @param sql UPDATE语句
     * @return 表名
     */
    @Override
    public String getTableName(String sql) {
        return getTableName(sql, LOWER_SET);
    }

    /**
     * 获取非tb_开头的表名开始索引
     *
     * @param sql SQL字符串
     * @return 索引位置
     */
    @Override
    protected int getSpecialTbIndex(String sql) {
        return getSpecialTbIndex(sql, LOWER_UPDATE);
    }

    /**
     * 注册当前处理器到SqlCommandAdapter
     *
     * @throws Exception 如果注册失败
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SqlCommandAdapter.register(this);
    }
}