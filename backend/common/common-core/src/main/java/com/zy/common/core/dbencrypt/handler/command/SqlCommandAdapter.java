package com.zy.common.core.dbencrypt.handler.command;

import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL命令适配器
 * <p>
 * 负责管理和分发不同SQL命令类型的处理逻辑
 *
 * @author zzy
 * @date 2026/05/04
 */
public class SqlCommandAdapter {

    /**
     * SQL命令类型与处理器的映射
     */
    private static final Map<String, SqlCommandHandler> handlerMap = new HashMap<>();

    private SqlCommandAdapter() {
    }

    /**
     * 注册SQL命令处理器
     *
     * @param handler SQL命令处理器
     */
    public static void register(SqlCommandHandler handler) {
        handlerMap.put(handler.sqlCommandType().name(), handler);
    }

    /**
     * 根据SQL命令类型获取对应的处理器
     *
     * @param sqlCommandType SQL命令类型
     * @return SQL命令处理器
     */
    private static SqlCommandHandler get(SqlCommandType sqlCommandType) {
        return handlerMap.get(sqlCommandType.name());
    }

    /**
     * 从SQL语句中提取表名
     *
     * @param sqlCommandType SQL命令类型
     * @param sql            SQL语句
     * @return 表名
     */
    public static String getTableName(SqlCommandType sqlCommandType, String sql) {
        SqlCommandHandler handler = get(sqlCommandType);
        if (handler == null) {
            return null;
        }
        return handler.getTableName(sql);
    }

    /**
     * 获取需要加密的参数索引及对应的加密规则
     *
     * @param sqlCommandType SQL命令类型
     * @param sql            SQL语句
     * @param columns        表的列加密规则映射
     * @return 参数索引与加密规则的映射
     */
    public static Map<Integer, DbEncryptColumnRule> getNeedEncryptParamIndexRule(SqlCommandType sqlCommandType, String sql, Map<String, DbEncryptColumnRule> columns) {
        SqlCommandHandler handler = get(sqlCommandType);
        if (handler == null) {
            return Collections.emptyMap();
        }
        return handler.getNeedEncryptParamIndexRule(sql, columns);
    }
}
