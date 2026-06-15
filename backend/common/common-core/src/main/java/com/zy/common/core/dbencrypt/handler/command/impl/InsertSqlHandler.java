package com.zy.common.core.dbencrypt.handler.command.impl;

import com.zy.common.core.dbencrypt.bean.DbEncryptionConstant;
import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import com.zy.common.core.dbencrypt.handler.command.SqlCommandAdapter;
import com.zy.common.core.dbencrypt.handler.command.SqlCommandHandler;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * INSERT SQL命令处理器
 * <p>
 * 负责解析INSERT语句，提取表名和需要加密的参数索引
 *
 * @author zzy
 * @date 2026/05/04
 */
@Configuration
@ConditionalOnClass({SqlSessionTemplate.class, SqlSessionFactoryBean.class, SqlSessionFactory.class})
public class InsertSqlHandler extends AbstractSqlCommandHandler implements SqlCommandHandler {

    /**
     * 获取SQL命令类型
     *
     * @return INSERT命令类型
     */
    @Override
    public SqlCommandType sqlCommandType() {
        return SqlCommandType.INSERT;
    }

    /**
     * 从INSERT SQL中提取表名
     *
     * @param sql INSERT语句
     * @return 表名
     */
    @Override
    public String getTableName(String sql) {
        return getTableName(sql, DbEncryptionConstant.LEFT_BRACKET);
    }

    /**
     * 获取非tb_开头的表名开始索引
     *
     * @param sql SQL字符串
     * @return 索引位置
     */
    @Override
    protected int getSpecialTbIndex(String sql) {
        return getSpecialTbIndex(sql, LOWER_INTO);
    }

    /**
     * 获取INSERT语句中需要加密的参数索引及对应的加密规则
     * <p>
     * 解析INSERT语句的列名和值，匹配需要加密的列
     *
     * @param sql     INSERT语句
     * @param columns 表的列加密规则映射
     * @return 参数索引与加密规则的映射
     */
    @Override
    public Map<Integer, DbEncryptColumnRule> getNeedEncryptParamIndexRule(String sql, Map<String, DbEncryptColumnRule> columns) {
        Map<Integer, DbEncryptColumnRule> indexRuleMap = new LinkedHashMap<>();
        if (StringUtils.isBlank(sql)) {
            return indexRuleMap;
        }
        int leftBracketIndex = sql.indexOf(DbEncryptionConstant.LEFT_BRACKET);
        int rightBracketIndex = sql.indexOf(DbEncryptionConstant.RIGHT_BRACKET);
        if (leftBracketIndex < 0 || rightBracketIndex < 0) {
            return indexRuleMap;
        }
        String[] split = sql.substring(leftBracketIndex + DbEncryptionConstant.LEFT_BRACKET.length(), rightBracketIndex).split(DbEncryptionConstant.COMMA);
        Map<Integer, DbEncryptColumnRule> nameIndexRuleMap = new LinkedHashMap<>();
        for (int i = 0; i < split.length; i++) {
            String name = split[i];
            DbEncryptColumnRule rule = columns.get(name.trim());
            if (rule != null) {
                nameIndexRuleMap.put(i, rule);
            }
        }
        if (MapUtils.isEmpty(nameIndexRuleMap)) {
            return indexRuleMap;
        }
        int size = split.length;
        int valueIndex = 0;
        rightBracketIndex = sql.indexOf(DbEncryptionConstant.RIGHT_BRACKET, rightBracketIndex + 1);
        while (rightBracketIndex >= 0) {
            for (Map.Entry<Integer, DbEncryptColumnRule> entry : nameIndexRuleMap.entrySet()) {
                indexRuleMap.put((valueIndex * size) + entry.getKey(), entry.getValue());
            }
            rightBracketIndex = sql.indexOf(DbEncryptionConstant.RIGHT_BRACKET, rightBracketIndex + 1);
            valueIndex++;
        }
        return indexRuleMap;
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