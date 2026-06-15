package com.zy.common.core.dbencrypt.handler.command;

import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * SQL命令处理器接口
 * <p>
 * 定义不同SQL命令类型（SELECT、INSERT、UPDATE、DELETE）的处理规范
 *
 * @author zzy
 * @date 2026/05/04
 */
public interface SqlCommandHandler extends InitializingBean {

    /**
     * 获取SQL命令类型
     *
     * @return SQL命令类型
     */
    SqlCommandType sqlCommandType();

    /**
     * 从SQL语句中提取表名
     *
     * @param sql SQL语句
     * @return 表名
     */
    String getTableName(String sql);

    /**
     * 获取需要加密的参数索引及对应的加密规则
     *
     * @param sql     SQL语句
     * @param columns 表的列加密规则映射
     * @return 参数索引与加密规则的映射
     */
    Map<Integer, DbEncryptColumnRule> getNeedEncryptParamIndexRule(String sql, Map<String, DbEncryptColumnRule> columns);
}
