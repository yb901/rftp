package com.zy.common.core.dbencrypt.handler.command.impl;

import com.zy.common.core.dbencrypt.bean.DbEncryptionConstant;
import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import com.zy.common.core.dbencrypt.handler.command.SqlCommandHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL命令处理器抽象基类
 * <p>
 * 提供SQL解析和表名提取的通用方法实现
 *
 * @author zzy
 * @date 2026/05/04
 */
public abstract class AbstractSqlCommandHandler implements SqlCommandHandler {

    /**
     * SET关键字（小写）
     */
    protected static final String LOWER_SET = "set";
    /**
     * WHERE关键字（小写）
     */
    protected static final String LOWER_WHERE = "where";
    /**
     * FROM关键字（小写）
     */
    protected static final String LOWER_FROM = "from ";
    /**
     * UPDATE关键字（小写）
     */
    protected static final String LOWER_UPDATE = "update ";
    /**
     * INTO关键字（小写）
     */
    protected static final String LOWER_INTO = "into ";
    /**
     * 空格分隔符
     */
    protected static final String SPACE = " ";
    /**
     * 换行符
     */
    protected static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * AND关键字（小写）
     */
    protected static final String LOWER_AND = "and";
    /**
     * IN关键字（小写）
     */
    protected static final String LOWER_IN = "in";
    /**
     * CASE关键字（小写）
     */
    protected static final String LOWER_CASE = "case";
    /**
     * WHEN关键字（小写）
     */
    protected static final String LOWER_WHEN = "when";

    /**
     * 获取非tb_开头的表名开始索引
     *
     * @param sql SQL字符串
     * @return 索引位置
     */
    protected abstract int getSpecialTbIndex(String sql);

    /**
     * 获取特殊表名的起始索引
     *
     * @param sql       SQL字符串
     * @param separator 分隔符
     * @return 索引位置
     */
    protected int getSpecialTbIndex(String sql, String separator) {
        int index = getIndexIgnoreCase(sql, separator);
        return index + separator.length();
    }

    /**
     * 从SQL中提取表名
     *
     * @param sql        SQL语句
     * @param separators 分隔符数组，用于确定表名结束位置
     * @return 表名
     */
    protected String getTableName(String sql, String... separators) {
        if (sql == null || sql.isEmpty()) {
            return null;
        }
        int tbIndex = getTbIndex(sql);
        if (tbIndex < 0) {
            return null;
        }
        int index = getTableNameEndIndex(sql, tbIndex, separators);
        if (index < 0) {
            return sql.substring(tbIndex);
        }
        if (tbIndex >= index) {
            return null;
        }
        String substring = sql.substring(tbIndex, index);
        return substring.trim();
    }

    /**
     * 获取表名起始索引
     *
     * @param sql SQL字符串
     * @return 表名起始索引
     */
    private int getTbIndex(String sql) {
        int tbIndex = sql.indexOf("tb_");
        if (tbIndex >= 0) {
            return tbIndex;
        }
        return getSpecialTbIndex(sql);
    }

    /**
     * 获取表名结束索引
     *
     * @param sql        SQL字符串
     * @param fromIndex  起始索引
     * @param separators 分隔符数组
     * @return 表名结束索引
     */
    private int getTableNameEndIndex(String sql, int fromIndex, String... separators) {
        int minIndex = -1;
        for (String separator : separators) {
            int index = getIndexIgnoreCase(sql, separator, fromIndex);
            if (minIndex < 0) {
                minIndex = index;
            }
            if (index > 0 && index < minIndex) {
                minIndex = index;
            }
        }
        return minIndex;
    }

    /**
     * 忽略大小写查找子串位置
     *
     * @param str       源字符串
     * @param separator 分隔符
     * @param fromIndex 起始位置
     * @return 子串位置
     */
    private static int getIndexIgnoreCase(String str, String separator, int fromIndex) {
        return str.toLowerCase().indexOf(separator.toLowerCase(), fromIndex);
    }

    /**
     * 忽略大小写查找子串位置
     *
     * @param str       源字符串
     * @param separator 分隔符
     * @return 子串位置
     */
    protected static int getIndexIgnoreCase(String str, String separator) {
        return str.toLowerCase().indexOf(separator.toLowerCase());
    }

    /**
     * 获取需要加密的参数索引及对应的加密规则
     *
     * @param sql     SQL语句
     * @param columns 表的列加密规则映射
     * @return 参数索引与加密规则的映射
     */
    @Override
    public Map<Integer, DbEncryptColumnRule> getNeedEncryptParamIndexRule(String sql, Map<String, DbEncryptColumnRule> columns) {
        Map<Integer, DbEncryptColumnRule> indexRuleMap = new LinkedHashMap<>();
        if (StringUtils.isBlank(sql)) {
            return indexRuleMap;
        }
        int index = putIndexRuleBySet(sql, columns, indexRuleMap);
        putIndexRuleByWhere(sql, index, columns, indexRuleMap);
        return indexRuleMap;
    }

    /**
     * 处理SET子句中的加密参数
     *
     * @param sql          SQL语句
     * @param columns      列加密规则映射
     * @param indexRuleMap 索引规则映射
     * @return 当前处理的参数索引
     */
    protected static int putIndexRuleBySet(String sql, Map<String, DbEncryptColumnRule> columns, Map<Integer, DbEncryptColumnRule> indexRuleMap) {
        int index = 0;
        int setIndex = getIndexIgnoreCase(sql, LOWER_SET);
        if (setIndex < 0) {
            return index;
        }
        int whereIndex = getIndexIgnoreCase(sql, LOWER_WHERE);
        if (whereIndex < 0) {
            return index;
        }
        String setSql = sql.substring(setIndex + LOWER_SET.length(), whereIndex);
        String[] split = setSql.split(DbEncryptionConstant.COMMA);
        for (String s : split) {
            int questionMarkIndex = s.indexOf(DbEncryptionConstant.QUESTION_MARK);
            if (questionMarkIndex < 0) {
                continue;
            }
            index++;
            int addIndex = putSetValue(s, index, questionMarkIndex, columns, indexRuleMap);
            index = index + addIndex;
        }
        return index;
    }

    /**
     * 处理SET子句中的单个值
     *
     * @param s                 SET子句片段
     * @param index             参数索引
     * @param questionMarkIndex 问号位置
     * @param columns           列加密规则映射
     * @param indexRuleMap      索引规则映射
     * @return 追加的索引增量
     */
    private static int putSetValue(String s, int index, int questionMarkIndex, Map<String, DbEncryptColumnRule> columns, Map<Integer, DbEncryptColumnRule> indexRuleMap) {
        int equalSignIndex = s.indexOf(DbEncryptionConstant.SEPARATOR_EQUAL_SIGN);
        if (equalSignIndex < 0) {
            return 0;
        }
        int caseIndex = getIndexIgnoreCase(s, LOWER_CASE);
        if (caseIndex < 0) {
            putEqualSign(s, index, questionMarkIndex, equalSignIndex, columns, indexRuleMap);
            return 0;
        }
        putCase(s, index, questionMarkIndex, equalSignIndex, caseIndex, columns, indexRuleMap);
        int questionCount = getQuestionCount(s);
        return questionCount - 1;
    }

    /**
     * 统计字符串中问号的数量
     *
     * @param str 字符串
     * @return 问号数量
     */
    private static int getQuestionCount(String str) {
        int count = 0;
        char question = DbEncryptionConstant.QUESTION_MARK.toCharArray()[0];
        for (char c : str.toCharArray()) {
            if (c == question) {
                count++;
            }
        }
        return count;
    }

    /**
     * 处理WHERE子句中的加密参数
     *
     * @param sql          SQL语句
     * @param index        当前索引
     * @param columns      列加密规则映射
     * @param indexRuleMap 索引规则映射
     * @return 处理后的索引
     */
    protected static int putIndexRuleByWhere(String sql, int index, Map<String, DbEncryptColumnRule> columns, Map<Integer, DbEncryptColumnRule> indexRuleMap) {
        int whereIndex = getIndexIgnoreCase(sql, LOWER_WHERE);
        if (whereIndex < 0) {
            return index;
        }
        String sqlAfterWhere = sql.substring(whereIndex + LOWER_WHERE.length());
        String[] split = sqlAfterWhere.split(LOWER_AND);
        for (String s : split) {
            int questionMarkIndex = s.indexOf(DbEncryptionConstant.QUESTION_MARK);
            if (questionMarkIndex < 0) {
                continue;
            }
            index++;
            int equalSignIndex = s.indexOf(DbEncryptionConstant.SEPARATOR_EQUAL_SIGN);
            if (equalSignIndex >= 0) {
                putEqualSign(s, index, questionMarkIndex, equalSignIndex, columns, indexRuleMap);
                continue;
            }
            int inIndex = getIndexIgnoreCase(s, LOWER_IN);
            if (inIndex >= 0) {
                index = putIn(s, index, questionMarkIndex, inIndex, columns, indexRuleMap);
            }
        }
        return index;
    }

    /**
     * 处理IN子句中的加密参数
     *
     * @param s                 IN子句片段
     * @param index             参数索引
     * @param questionMarkIndex 问号位置
     * @param inIndex           IN关键字位置
     * @param columns           列加密规则映射
     * @param indexRuleMap      索引规则映射
     * @return 处理后的索引
     */
    private static int putIn(String s, int index, int questionMarkIndex, int inIndex, Map<String, DbEncryptColumnRule> columns, Map<Integer, DbEncryptColumnRule> indexRuleMap) {
        if (questionMarkIndex < inIndex) {
            return index;
        }
        s = s.substring(0, s.indexOf(DbEncryptionConstant.RIGHT_BRACKET));
        String columnName = s.substring(0, inIndex).trim();
        DbEncryptColumnRule rule = columns.get(columnName);
        if (rule == null) {
            return index;
        }
        while (questionMarkIndex >= 0) {
            indexRuleMap.put(index - 1, rule);
            questionMarkIndex = s.indexOf(DbEncryptionConstant.QUESTION_MARK, questionMarkIndex + 1);
            if (questionMarkIndex >= 0) {
                index++;
            }
        }
        return index;
    }

    /**
     * 处理等值比较的加密参数
     *
     * @param s                 片段
     * @param index             参数索引
     * @param questionMarkIndex 问号位置
     * @param equalSignIndex    等号位置
     * @param columns           列加密规则映射
     * @param indexRuleMap      索引规则映射
     */
    private static void putEqualSign(String s, int index, int questionMarkIndex, int equalSignIndex, Map<String, DbEncryptColumnRule> columns, Map<Integer, DbEncryptColumnRule> indexRuleMap) {
        if (questionMarkIndex < equalSignIndex) {
            return;
        }
        String columnName = s.substring(0, equalSignIndex).trim();
        DbEncryptColumnRule rule = columns.get(columnName);
        if (rule == null) {
            return;
        }
        indexRuleMap.put(index - 1, rule);
    }

    /**
     * 处理CASE语句中的加密参数
     *
     * @param s                 片段
     * @param index             参数索引
     * @param questionMarkIndex 问号位置
     * @param equalSignIndex    等号位置
     * @param caseIndex         CASE关键字位置
     * @param columns           列加密规则映射
     * @param indexRuleMap      索引规则映射
     */
    private static void putCase(String s, int index, int questionMarkIndex, int equalSignIndex, int caseIndex, Map<String, DbEncryptColumnRule> columns, Map<Integer, DbEncryptColumnRule> indexRuleMap) {
        if (questionMarkIndex < equalSignIndex) {
            return;
        }
        String columnName = s.substring(0, equalSignIndex).trim();
        DbEncryptColumnRule rule = columns.get(columnName);
        String whenColumnName = s.substring(caseIndex + LOWER_CASE.length(), getIndexIgnoreCase(s, LOWER_WHEN)).trim();
        DbEncryptColumnRule whenRule = columns.get(whenColumnName);
        if (rule == null && whenRule == null) {
            return;
        }
        boolean isWhen = true;
        while (questionMarkIndex >= 0) {
            DbEncryptColumnRule r = isWhen ? whenRule : rule;
            if (r != null) {
                indexRuleMap.put(index - 1, r);
            }
            questionMarkIndex = s.indexOf(DbEncryptionConstant.QUESTION_MARK, questionMarkIndex + 1);
            if (questionMarkIndex >= 0) {
                index++;
            }
            isWhen = !isWhen;
        }
    }
}