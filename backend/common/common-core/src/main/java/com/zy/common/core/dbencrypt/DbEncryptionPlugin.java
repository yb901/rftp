package com.zy.common.core.dbencrypt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.zy.common.core.dbencrypt.bean.DbEncryptionConstant;
import com.zy.common.core.dbencrypt.bean.PropertyValueBean;
import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import com.zy.common.core.dbencrypt.config.DbEncryptTableRule;
import com.zy.common.core.dbencrypt.handler.command.SqlCommandAdapter;
import com.zy.common.core.dbencrypt.handler.encrypt.EncryptionDecryptionAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import org.springframework.lang.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据库加解密插件
 * <p>
 * MyBatis拦截器，用于在数据库操作时对敏感字段进行加解密处理。
 * 支持INSERT、UPDATE、SELECT、DELETE操作，支持嵌套JSON对象的加密。
 *
 * @author zzy
 * @date 2026/05/04
 */
@Slf4j
@Intercepts({
        @Signature(method = "update", type = Executor.class, args = {MappedStatement.class, Object.class}),
        @Signature(method = "query", type = Executor.class, args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(method = "query", type = Executor.class, args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class DbEncryptionPlugin implements Interceptor {

    /**
     * 表名与加密规则的映射
     */
    private final Map<String, DbEncryptTableRule> tables = new LinkedHashMap<>();

    /**
     * 密钥映射：key为"加密方式+版本号"，value为密钥
     */
    private final Map<String, String> secretMap = new LinkedHashMap<>();

    /**
     * foreach参数前缀
     */
    private static final String FEACH = "__feach_";

    /**
     * SELECT关键字（小写）
     */
    private static final String LOWER_SELECT = "select";
    /**
     * FROM关键字（小写）
     */
    private static final String LOWER_FROM = "from";

    /**
     * 日志级别：close/error/info/debug
     */
    private String logLevel;

    /**
     * 拦截方法，对SQL参数进行加密，对查询结果进行解密
     *
     * @param invocation 调用信息
     * @return 处理结果
     * @throws Throwable 如果处理过程中发生异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        if (MapUtils.isEmpty(tables) || args[1] == null) {
            return invocation.proceed();
        }
        long start = System.currentTimeMillis();
        boolean alreadyEncrypt = false;
        MappedStatement mappedStatement;
        String classAndMethod;
        SqlCommandType sqlCommandType;
        BoundSql boundSql = null;
        DbEncryptTableRule columns;
        try {
            try {
                mappedStatement = getMappedStatement(invocation.getTarget(), args[0]);
                // 得到 类名-方法
                classAndMethod = getClassAndMethod(mappedStatement);
                // 获取命令类型
                sqlCommandType = getSqlCommandType(mappedStatement, classAndMethod);
                // 获取sql
                boundSql = getBoundSql(mappedStatement, args, classAndMethod);
                // 获取表对应的加密配置
                columns = getDbEncryptTableRule(sqlCommandType, boundSql, classAndMethod);
            } catch (Throwable e) {
                if (errorLogEnable()) {
                    log.error("DbEncryptionPlugin, parse error, boundSql={}, e:", boundSql == null ? null : boundSql.getSql(), e);
                }
                return invocation.proceed();
            }
            if (columns == null || columns.getColumns() == null || columns.getColumns().isEmpty()) {
                // 该表无加密字段
                return invocation.proceed();
            }
            try {
                // 加密参数
                Map<String, PropertyValueBean> paramsRuleMap = getNeedEncryptParamsRule(mappedStatement, sqlCommandType, boundSql, columns.getColumns(), classAndMethod);
                Object encrypt = encrypt(paramsRuleMap, args[1], classAndMethod);
                alreadyEncrypt = true;
                args[1] = encrypt;
            } catch (Throwable e) {
                if (errorLogEnable()) {
                    log.error("DbEncryptionPlugin, encrypt error, boundSql={}, e:", boundSql.getSql(), e);
                }
                return invocation.proceed();
            }
        } finally {
            if (infoLogEnable()) {
                log.info("DbEncryptionPlugin, boundSql={}, args={}, alreadyEncrypt={}, cost={}ms", boundSql.getSql(), JSON.toJSONString(args[1]), alreadyEncrypt, System.currentTimeMillis() - start);
            }
        }
        Object proceed = invocation.proceed();
        if (proceed == null || isPrimitive(proceed.getClass())) {
            return proceed;
        }
        try {
            // 解密结果
            return decrypt(proceed, mappedStatement, boundSql, columns, classAndMethod);
        } catch (RuntimeException e) {
            if (warnLogEnable()) {
                log.warn("DbEncryptionPlugin, decrypt error, boundSql={}, e:", boundSql.getSql(), e);
            }
            return proceed;
        } catch (Throwable e) {
            if (errorLogEnable()) {
                log.error("DbEncryptionPlugin, decrypt error, boundSql={}, e:", boundSql.getSql(), e);
            }
            return proceed;
        }
    }

    /**
     * 获取MappedStatement
     *
     * @param target 执行目标
     * @param arg    参数
     * @return MappedStatement
     */
    private MappedStatement getMappedStatement(Object target, Object arg) {
        if (!(target instanceof Executor)) {
            // 仅处理Executor
            throw new RuntimeException("target非Executor");
        }
        if (!(arg instanceof MappedStatement)) {
            // 理论上第一个参数都是 MappedStatement
            throw new RuntimeException("第一参数非MappedStatement");
        }
        return (MappedStatement) arg;
    }

    /**
     * 获取类名和方法名
     *
     * @param mappedStatement MappedStatement
     * @return 类名.方法名
     */
    private String getClassAndMethod(MappedStatement mappedStatement) {
        String[] strArr = mappedStatement.getId().split("\\.");
        return strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];
    }

    /**
     * 获取SQL命令类型
     *
     * @param mappedStatement MappedStatement
     * @param classAndMethod  类名.方法名
     * @return SQL命令类型
     */
    private SqlCommandType getSqlCommandType(MappedStatement mappedStatement, String classAndMethod) throws RuntimeException {
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (sqlCommandType == null) {
            // 获取不到sql命令类型
            throw new RuntimeException(classAndMethod + " sqlCommandType为null");
        }
        return sqlCommandType;
    }

    /**
     * 获取BoundSql
     *
     * @param mappedStatement MappedStatement
     * @param args            参数数组
     * @param classAndMethod  类名.方法名
     * @return BoundSql
     */
    private BoundSql getBoundSql(MappedStatement mappedStatement, Object[] args, String classAndMethod) throws RuntimeException {
        BoundSql boundSql = mappedStatement.getBoundSql(args[1]);
        if (boundSql == null) {
            throw new RuntimeException(classAndMethod + " boundSql为null");
        }
        return boundSql;
    }

    /**
     * 获取表对应的加密配置
     *
     * @param sqlCommandType SQL命令类型
     * @param boundSql       BoundSql
     * @param classAndMethod 类名.方法名
     * @return 表加密规则
     */
    private DbEncryptTableRule getDbEncryptTableRule(SqlCommandType sqlCommandType, BoundSql boundSql, String classAndMethod) throws RuntimeException {
        // 获取本次sql的表名
        String tableName = SqlCommandAdapter.getTableName(sqlCommandType, boundSql.getSql());
        if (tableName == null || tableName.isEmpty()) {
            throw new RuntimeException(classAndMethod + " 获取tableName失败");
        }
        // 获取表对应的加密配置
        return tables.get(tableName);
    }

    /**
     * 获取需要加密的参数及其规则
     *
     * @param mappedStatement MappedStatement
     * @param sqlCommandType  SQL命令类型
     * @param boundSql        BoundSql
     * @param columns         列加密规则映射
     * @param classAndMethod  类名.方法名
     * @return 参数名与属性值及规则映射
     */
    private Map<String, PropertyValueBean> getNeedEncryptParamsRule(MappedStatement mappedStatement, SqlCommandType sqlCommandType, BoundSql boundSql, Map<String, DbEncryptColumnRule> columns, String classAndMethod) throws RuntimeException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            if (warnLogEnable()) {
                log.warn("DbEncryptionPlugin, parameterMappings is null or empty, sql={}, classAndMethod={}", boundSql.getSql(), classAndMethod);
            }
            return Collections.emptyMap();
        }
        Map<Integer, DbEncryptColumnRule> paramIndexRuleMap = SqlCommandAdapter.getNeedEncryptParamIndexRule(sqlCommandType, boundSql.getSql(), columns);
        if (paramIndexRuleMap.isEmpty()) {
            return Collections.emptyMap();
        }
        MetaObject metaObject = null;
        Map<String, PropertyValueBean> paramRuleMap = new HashMap<>();
        for (Map.Entry<Integer, DbEncryptColumnRule> entry : paramIndexRuleMap.entrySet()) {
            String property = getProperty(entry.getKey(), parameterMappings, boundSql);
            if (property == null) {
                continue;
            }
            metaObject = put(mappedStatement, classAndMethod, paramRuleMap, property, entry.getValue(), metaObject, boundSql);
        }
        return paramRuleMap;
    }

    /**
     * 将参数及其规则放入映射表
     *
     * @param mappedStatement MappedStatement
     * @param classAndMethod  类名.方法名
     * @param paramRuleMap    参数规则映射
     * @param property        参数属性名
     * @param rule            加密规则
     * @param metaObject      MetaObject
     * @param boundSql        BoundSql
     * @return MetaObject
     */
    private MetaObject put(MappedStatement mappedStatement, String classAndMethod, Map<String, PropertyValueBean> paramRuleMap, String property, DbEncryptColumnRule rule, MetaObject metaObject, BoundSql boundSql) throws RuntimeException {
        Object parameterObject = boundSql.getParameterObject();
        if (!property.startsWith(FEACH)) {
            putNotFeach(paramRuleMap, property, rule);
            return metaObject;
        }
        int separatorIndex = property.indexOf(".");
        if (separatorIndex >= 0) {
            putFeachObj(paramRuleMap, property, rule, separatorIndex);
            return metaObject;
        }
        if (metaObject == null) {
            metaObject = getMetaObject(mappedStatement, classAndMethod, parameterObject);
        }
        putFeachNotObj(paramRuleMap, property, rule, metaObject, boundSql);
        return metaObject;
    }

    /**
     * 加密参数
     *
     * @param paramsRuleMap   参数规则映射
     * @param parameterObject 参数对象
     * @param classAndMethod  类名.方法名
     * @return 加密后的参数对象
     */
    private Object encrypt(Map<String, PropertyValueBean> paramsRuleMap, Object parameterObject, String classAndMethod) throws RuntimeException {
        if (parameterObject == null || MapUtils.isEmpty(paramsRuleMap)) {
            return parameterObject;
        }
        if (parameterObject instanceof String) {
            return encryptString((String) parameterObject, paramsRuleMap, classAndMethod);
        }
        Class<?> clazz = parameterObject.getClass();
        if (isPrimitive(clazz)) {
            // 非字符串类型的基本类型
            throw new RuntimeException(classAndMethod + " 需要加密，但入参为非字符串类型的基本类型");
        }
        if (parameterObject instanceof Map) {
            return encryptMap(toMap(parameterObject), paramsRuleMap, classAndMethod);
        }
        if (parameterObject instanceof Collection) {
            // 数组类型
            return encryptCollection(toCollection(parameterObject), paramsRuleMap, classAndMethod);
        }
        // 对象类型
        return encryptObject(parameterObject, paramsRuleMap);
    }

    /**
     * 加密字符串类型参数
     *
     * @param parameterObject 参数字符串
     * @param paramsRuleMap   参数规则映射
     * @param classAndMethod  类名.方法名
     * @return 加密后的字符串
     */
    private String encryptString(String parameterObject, Map<String, PropertyValueBean> paramsRuleMap, String classAndMethod) throws RuntimeException {
        if (paramsRuleMap.size() != 1) {
            throw new RuntimeException(classAndMethod + " 需要加密，入参为String，但是paramsRuleMap.size!=1");
        }
        PropertyValueBean bean = paramsRuleMap.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (bean == null) {
            return parameterObject;
        }
        DbEncryptColumnRule rule = bean.getRule();
        return EncryptionDecryptionAdapter.encryptString(parameterObject, rule, secretMap);
    }

    /**
     * 加密Map类型参数
     *
     * @param parameterObject Map参数
     * @param paramsRuleMap   参数规则映射
     * @param classAndMethod  类名.方法名
     * @return 加密后的Map
     */
    private Map<Object, Object> encryptMap(Map<Object, Object> parameterObject, Map<String, PropertyValueBean> paramsRuleMap, String classAndMethod) throws RuntimeException {
        if (MapUtils.isEmpty(parameterObject)) {
            return parameterObject;
        }
        Map<Object, Object> newValueMap = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : parameterObject.entrySet()) {
            encryptMap(entry, newValueMap, paramsRuleMap, classAndMethod);
        }
        if (MapUtils.isNotEmpty(newValueMap)) {
            parameterObject.putAll(newValueMap);
        }
        return parameterObject;
    }

    /**
     * 处理Map中的单个条目
     *
     * @param entry          Map条目
     * @param newValueMap    新值映射
     * @param paramsRuleMap  参数规则映射
     * @param classAndMethod 类名.方法名
     */
    private void encryptMap(Map.Entry<Object, Object> entry, Map<Object, Object> newValueMap, Map<String, PropertyValueBean> paramsRuleMap, String classAndMethod) throws RuntimeException {
        Object key = entry.getKey();
        Object value = entry.getValue();
        if (value == null) {
            return;
        }
        if (value instanceof Collection) {
            // 集合
            newValueMap.put(key, encryptMapCollection(key, toCollection(value), paramsRuleMap, classAndMethod));
            return;
        }
        if (value instanceof Map) {
            // map套map，不支持
            return;
        }
        if (isPrimitive(value.getClass())) {
            // 基础类型，不支持
            return;
        }
        // 字符串或者json对象，不太可能是普通对象
        PropertyValueBean bean = paramsRuleMap.get(key.toString());
        if (bean == null) {
            return;
        }
        // 字符串/字符串json/对象json
        Pair<Boolean, Object> pair = encryptOrDecryptJson(value, bean.getRule(), true);
        if (BooleanUtils.isNotTrue(pair.getLeft())) {
            // 未加密
            return;
        }
        newValueMap.put(key, pair.getValue());
    }

    /**
     * 处理Map中集合类型的值
     *
     * @param key            Map键
     * @param collection     集合值
     * @param paramsRuleMap  参数规则映射
     * @param classAndMethod 类名.方法名
     * @return 处理后的集合
     */
    private Object encryptMapCollection(Object key, Collection<Object> collection, Map<String, PropertyValueBean> paramsRuleMap, String classAndMethod) throws RuntimeException {
        if (CollectionUtils.isEmpty(collection)) {
            return collection;
        }
        PropertyValueBean bean = paramsRuleMap.get(key.toString());
        if (bean != null) {
            // 当前集合对象，可能是：json字符串/普通字符串/json对象
            // 获取到bean了，不应该是普通对象
            return encryptOrDecryptCollectionJson(collection, bean.getRule(), true, true);
        }
        // 未获取到bean
        // 当前集合对象，可能是：json字符串/普通字符串/普通对象
        // 不应该是json对象
        Object value = collection.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (value == null) {
            return collection;
        }
        if (value instanceof String) {
            // json字符串/普通字符串
            DbEncryptColumnRule rule = getCollectionStringRule(collection, paramsRuleMap, classAndMethod);
            return encryptOrDecryptCollectionString(collection, rule, true, true);
        }
        // 普通对象
        return encryptCollectionObject(collection, paramsRuleMap);
    }

    /**
     * 解密查询结果
     *
     * @param proceed         查询结果
     * @param mappedStatement MappedStatement
     * @param boundSql        BoundSql
     * @param columns         表加密规则
     * @param classAndMethod  类名.方法名
     * @return 解密后的结果
     */
    private Object decrypt(Object proceed, MappedStatement mappedStatement, BoundSql boundSql, DbEncryptTableRule columns, String classAndMethod) throws RuntimeException {
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        if (CollectionUtils.isEmpty(resultMaps)) {
            // 无需解密
            return proceed;
        }
        long start = System.currentTimeMillis();
        try {
            // 获取sql中的所有字段
            List<String> columnList = getResultColumns(boundSql.getSql());
            // 获取对象名称里对应的加密规则
            Map<String, DbEncryptColumnRule> propertyRuleMap = getPropertyRuleMap(resultMaps, columns.getColumns(), columnList);
            if (MapUtils.isEmpty(propertyRuleMap)) {
                return proceed;
            }
            // 解密结果
            return decrypt(proceed, propertyRuleMap, classAndMethod);
        } finally {
            if (infoLogEnable()) {
                log.info("DbEncryptionPlugin, decrypt, boundSql={}, cost={}ms", boundSql.getSql(), System.currentTimeMillis() - start);
            }
        }
    }

    /**
     * 解密结果对象
     *
     * @param result          结果对象
     * @param propertyRuleMap 属性加密规则映射
     * @param classAndMethod  类名.方法名
     * @return 解密后的结果
     */
    private Object decrypt(Object result, Map<String, DbEncryptColumnRule> propertyRuleMap, String classAndMethod) throws RuntimeException {
        if ((result instanceof String) && propertyRuleMap.size() != 1) {
            throw new RuntimeException(classAndMethod + " 查询一个字段，但是有多个字段要解密");
        }
        if (result instanceof String) {
            // json字符串/普通字符串
            DbEncryptColumnRule firstRule = getFirstRule(propertyRuleMap);
            return encryptOrDecryptJsonString((String) result, firstRule, false);
        }
        if (result instanceof Collection) {
            return decryptCollection(toCollection(result), propertyRuleMap, classAndMethod);
        }
        if (result instanceof Map) {
            // 暂不支持map
            return result;
        }
        // 对象类型
        return decryptObject(result, propertyRuleMap);
    }

    /**
     * 解密集合结果
     *
     * @param collection      集合结果
     * @param propertyRuleMap 属性加密规则映射
     * @param classAndMethod  类名.方法名
     * @return 解密后的集合
     */
    private Collection<Object> decryptCollection(Collection<Object> collection, Map<String, DbEncryptColumnRule> propertyRuleMap, String classAndMethod) throws RuntimeException {
        if (CollectionUtils.isEmpty(collection)) {
            return collection;
        }
        Object value = collection.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (value == null) {
            return collection;
        }
        if (isPrimitive(value.getClass())) {
            return collection;
        }
        if ((value instanceof String) && propertyRuleMap.size() != 1) {
            throw new RuntimeException(classAndMethod + " 查询一个字段集合，但是有多个字段要解密");
        }
        if (value instanceof String) {
            // 普通字符串/json对象字符串/json数组字符串
            DbEncryptColumnRule firstRule = getFirstRule(propertyRuleMap);
            return encryptOrDecryptCollectionString(collection, firstRule, false, true);
        }
        if (value instanceof List) {
            return collection;
        }
        if (value instanceof Map) {
            return collection;
        }
        // 普通对象
        for (Object v : collection) {
            decryptObject(v, propertyRuleMap);
        }
        return collection;
    }

    /**
     * 获取第一个加密规则
     *
     * @param propertyRuleMap 属性加密规则映射
     * @return 第一个加密规则
     */
    private DbEncryptColumnRule getFirstRule(Map<String, DbEncryptColumnRule> propertyRuleMap) {
        return propertyRuleMap.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * 获取对象的所有非静态字段
     *
     * @param obj 对象
     * @return 字段列表
     */
    public static List<Field> getNotStaticField(Object obj) {
        List<Field> fieldList = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fieldList.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 解密对象类型的查询结果
     *
     * @param result          结果对象
     * @param propertyRuleMap 属性加密规则映射
     * @return 解密后的对象
     */
    private Object decryptObject(Object result, Map<String, DbEncryptColumnRule> propertyRuleMap) {
        try {
            List<Field> collect = getNotStaticField(result);
            for (Field field : collect) {
                String name = field.getName();
                DbEncryptColumnRule rule = propertyRuleMap.get(name);
                if (rule == null) {
                    continue;
                }
                encryptOrDecryptObject(rule, field, result, false);
            }
            return result;
        } catch (IllegalAccessException e) {
            return result;
        }
    }

    /**
     * 获取属性对应的加密规则映射
     *
     * @param resultMaps 结果映射列表
     * @param columns    列加密规则映射
     * @param columnList SQL查询的列列表
     * @return 属性与加密规则的映射
     */
    private Map<String, DbEncryptColumnRule> getPropertyRuleMap(List<ResultMap> resultMaps, Map<String, DbEncryptColumnRule> columns, List<String> columnList) {
        Map<String, DbEncryptColumnRule> propertyRuleMap = new HashMap<>();
        for (ResultMap resultMap : resultMaps) {
            List<ResultMapping> propertyResultMappings = resultMap.getPropertyResultMappings();
            if (CollectionUtils.isNotEmpty(propertyResultMappings)) {
                putPropertyResultMappings(propertyResultMappings, columns, propertyRuleMap);
                continue;
            }
            // 根据sql中的查询字段构建
            putByColumn(columnList, columns, propertyRuleMap);
        }
        return propertyRuleMap;
    }

    /**
     * 根据列名填充属性规则映射
     *
     * @param columnList      列名列表
     * @param columns         列加密规则映射
     * @param propertyRuleMap 属性规则映射
     */
    private void putByColumn(List<String> columnList, Map<String, DbEncryptColumnRule> columns, Map<String, DbEncryptColumnRule> propertyRuleMap) {
        if (CollectionUtils.isEmpty(columnList)) {
            return;
        }
        for (String column : columnList) {
            Pair<String, String> pair = parseColumn(column);
            DbEncryptColumnRule rule = columns.get(pair.getLeft());
            if (rule == null) {
                continue;
            }
            propertyRuleMap.put(pair.getRight(), rule);
        }
    }

    /**
     * 解析列名，返回数据库列名和属性名的配对
     *
     * @param column 列名
     * @return 列名和属性名的配对
     */
    private Pair<String, String> parseColumn(String column) {
        int spaceIndex = column.indexOf(DbEncryptionConstant.SPACE);
        String columnName;
        if (spaceIndex < 0) {
            columnName = column;
        } else {
            columnName = column.substring(0, spaceIndex);
        }
        int lastSpaceIndex = column.lastIndexOf(DbEncryptionConstant.SPACE);
        String property;
        if (lastSpaceIndex < 0) {
            // 没有空格，直接下划线转驼峰
            property = toHump(column);
        } else {
            // 有空格，取最后的一个单词
            property = column.substring(lastSpaceIndex + 1);
        }
        return Pair.of(columnName, property);
    }

    /**
     * 将下划线命名转换为驼峰命名
     *
     * @param column 列名
     * @return 驼峰命名字符串
     */
    private String toHump(String column) {
        String[] split = column.split(DbEncryptionConstant.UNDERLINE);
        if (split.length == 1) {
            return split[0];
        }
        StringBuilder property = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String part = split[i];
            if (i == 0) {
                property.append(part);
            } else {
                property.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
            }
        }
        return property.toString();
    }

    /**
     * 从SQL中提取查询的列
     *
     * @param sql SQL语句
     * @return 列名列表
     */
    private List<String> getResultColumns(String sql) {
        int selectIndex = sql.toLowerCase().indexOf(LOWER_SELECT);
        int fromIndex = sql.toLowerCase().indexOf(LOWER_FROM);
        if (selectIndex < 0 || fromIndex < 0 || selectIndex + LOWER_SELECT.length() >= fromIndex) {
            return Collections.emptyList();
        }
        String[] split = sql.substring(selectIndex + LOWER_SELECT.length(), fromIndex).split(DbEncryptionConstant.COMMA);
        return Arrays.stream(split).map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    /**
     * 根据ResultMapping填充属性规则映射
     *
     * @param propertyResultMappings 结果属性映射列表
     * @param columns                列加密规则映射
     * @param propertyRuleMap        属性规则映射
     */
    private void putPropertyResultMappings(List<ResultMapping> propertyResultMappings, Map<String, DbEncryptColumnRule> columns, Map<String, DbEncryptColumnRule> propertyRuleMap) {
        for (ResultMapping resultMapping : propertyResultMappings) {
            String column = resultMapping.getColumn();
            DbEncryptColumnRule rule = columns.get(column);
            if (rule == null) {
                continue;
            }
            propertyRuleMap.put(resultMapping.getProperty(), rule);
        }
    }

    /**
     * 加解密集合类型数据
     *
     * @param collection 集合
     * @param rule       加密规则
     * @param isEncrypt  是否为加密操作
     * @return 处理后的集合
     */
    private Collection<Object> encryptOrDecryptCollection(Collection<Object> collection, DbEncryptColumnRule rule, boolean isEncrypt) {
        if (CollectionUtils.isEmpty(collection)) {
            return collection;
        }
        Object value = collection.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (value == null) {
            return collection;
        }
        if (value instanceof String) {
            return encryptOrDecryptCollectionString(collection, rule, isEncrypt, false);
        }
        if (isPrimitive(value.getClass())) {
            // 基本类型
            return collection;
        }
        if (value instanceof Collection || value instanceof Map) {
            // 集合类型，暂不支持
            return collection;
        }
        if (CollectionUtils.isEmpty(rule.getNames())) {
            return collection;
        }
        return encryptOrDecryptCollectionJsonObj(toCollection(collection), rule, isEncrypt);
    }

    /**
     * 加密集合类型参数
     *
     * @param collection     集合
     * @param paramsRuleMap  参数规则映射
     * @param classAndMethod 类名.方法名
     * @return 加密后的集合
     */
    private Collection<Object> encryptCollection(Collection<Object> collection, Map<String, PropertyValueBean> paramsRuleMap, String classAndMethod) throws RuntimeException {
        if (CollectionUtils.isEmpty(collection)) {
            return collection;
        }
        Object value = collection.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (value == null) {
            return collection;
        }
        if (value instanceof Collection || value instanceof Map) {
            // 集合类型，暂不支持
            return collection;
        }
        if (isPrimitive(value.getClass())) {
            // 基本类型
            return collection;
        }
        if (value instanceof String) {
            DbEncryptColumnRule rule = getCollectionStringRule(collection, paramsRuleMap, classAndMethod);
            return encryptOrDecryptCollectionString(collection, rule, true, true);
        }
        // 对象类型
        return encryptCollectionObject(collection, paramsRuleMap);
    }

    /**
     * 加密集合中的对象类型元素
     *
     * @param collection    集合
     * @param paramsRuleMap 参数规则映射
     * @return 加密后的集合
     */
    private Collection<Object> encryptCollectionObject(Collection<Object> collection, Map<String, PropertyValueBean> paramsRuleMap) {
        for (Object v : collection) {
            encryptObject(v, paramsRuleMap);
        }
        return collection;
    }

    /**
     * 加密对象类型参数
     *
     * @param obj           对象
     * @param paramsRuleMap 参数规则映射
     * @return 加密后的对象
     */
    private Object encryptObject(Object obj, Map<String, PropertyValueBean> paramsRuleMap) {
        if (obj == null) {
            return null;
        }
        try {
            List<Field> fields = getNotStaticField(obj);
            for (Field field : fields) {
                encryptObject(field, obj, paramsRuleMap);
            }
            return obj;
        } catch (IllegalAccessException e) {
            return obj;
        }
    }

    /**
     * 加密对象中的单个字段
     *
     * @param field         字段
     * @param obj           对象
     * @param paramsRuleMap 参数规则映射
     * @throws IllegalAccessException 如果访问字段失败
     */
    private void encryptObject(Field field, Object obj, Map<String, PropertyValueBean> paramsRuleMap) throws IllegalAccessException {
        String name = field.getName();
        PropertyValueBean bean = paramsRuleMap.get(name);
        if (bean == null) {
            return;
        }
        DbEncryptColumnRule rule = bean.getRule();
        encryptOrDecryptObject(rule, field, obj, true);
    }

    /**
     * 加解密对象的字段
     *
     * @param rule      加密规则
     * @param field     字段
     * @param obj       对象
     * @param isEncrypt 是否为加密操作
     * @throws IllegalAccessException 如果访问字段失败
     */
    private void encryptOrDecryptObject(DbEncryptColumnRule rule, Field field, Object obj, boolean isEncrypt) throws IllegalAccessException {
        if (rule == null) {
            return;
        }
        fieldSetAccessible(field);
        Object value = field.get(obj);
        Pair<Boolean, Object> pair = encryptOrDecryptJson(value, rule, isEncrypt);
        if (BooleanUtils.isNotTrue(pair.getLeft())) {
            // 没有加解密
            return;
        }
        fieldSet(obj, field, pair.getRight());
    }

    /**
     * 处理JSON类型的数据
     *
     * @param value     值
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @return 是否成功及处理后的值
     */
    private Pair<Boolean, Object> encryptOrDecryptJson(Object value, DbEncryptColumnRule rule, boolean isEncrypt) {
        if (value == null) {
            return Pair.of(false, null);
        }
        if (value instanceof String) {
            return Pair.of(true, encryptOrDecryptJsonString((String) value, rule, isEncrypt));
        }
        if (isPrimitive(value.getClass()) || value instanceof Map) {
            return Pair.of(false, value);
        }
        if (value instanceof Collection) {
            return Pair.of(true, encryptOrDecryptCollection(toCollection(value), rule, isEncrypt));
        }
        if (CollectionUtils.isNotEmpty(rule.getNames())) {
            // 加密对象是json
            return Pair.of(true, encryptOrDecryptJsonObj(value, rule, isEncrypt));
        }
        return Pair.of(false, value);
    }

    /**
     * 处理JSON字符串
     *
     * @param value     JSON字符串
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @return 处理后的字符串
     */
    private String encryptOrDecryptJsonString(String value, DbEncryptColumnRule rule, boolean isEncrypt) {
        if (rule == null) {
            return value;
        }
        if (CollectionUtils.isEmpty(rule.getNames())) {
            // 非json
            return encryptOrDecryptString(isEncrypt, value, rule);
        }
        if (value.startsWith(DbEncryptionConstant.LEFT_MIDDLE_BRACKET)) {
            // 值是json数组
            return encryptOrDecryptJsonArray(value, rule, isEncrypt);
        }
        if (value.startsWith(DbEncryptionConstant.LEFT_CURLY_BRACKET)) {
            // 值是json对象
            return encryptOrDecryptJsonObject(value, rule, isEncrypt);
        }
        return encryptOrDecryptString(isEncrypt, value, rule);
    }

    /**
     * 处理JSON数组字符串
     *
     * @param value     JSON数组字符串
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @return 处理后的JSON数组字符串
     */
    private String encryptOrDecryptJsonArray(String value, DbEncryptColumnRule rule, boolean isEncrypt) {
        JSONArray array = JSON.parseArray(value);
        Object o = encryptOrDecryptCollectionJson(array, rule, isEncrypt, false);
        return JSON.toJSONString(o);
    }

    /**
     * 处理JSON对象字符串
     *
     * @param value     JSON对象字符串
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @return 处理后的JSON对象字符串
     */
    private String encryptOrDecryptJsonObject(String value, DbEncryptColumnRule rule, boolean isEncrypt) {
        JSONObject jsonObject = JSON.parseObject(value);
        encryptOrDecryptJsonMap(jsonObject, rule, isEncrypt);
        return jsonObject.toJSONString();
    }

    /**
     * 处理JSON集合
     *
     * @param value     JSON集合
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @param maybeJson 是否可能是JSON
     * @return 处理后的集合
     */
    private @Nullable Collection<Object> encryptOrDecryptCollectionJson(Collection<Object> value, DbEncryptColumnRule rule, boolean isEncrypt, boolean maybeJson) {
        if (value == null) {
            return null;
        }
        // json是数组
        Object first = value.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (first == null) {
            return value;
        }
        if (first instanceof String) {
            // 字符串数组
            return encryptOrDecryptCollectionString(value, rule, isEncrypt, maybeJson);
        }
        if (isPrimitive(first.getClass())) {
            // 基础类型不支持
            return value;
        }
        // json对象数组
        return encryptOrDecryptCollectionJsonObj(value, rule, isEncrypt);
    }

    /**
     * 处理JSON对象的Map
     *
     * @param jsonObject JSON对象
     * @param rule       加密规则
     * @param isEncrypt  是否为加密操作
     */
    private void encryptOrDecryptJsonMap(JSONObject jsonObject, DbEncryptColumnRule rule, boolean isEncrypt) {
        if (CollectionUtils.isEmpty(rule.getNames()) || MapUtils.isEmpty(jsonObject)) {
            return;
        }
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String name = entry.getKey();
            if (!rule.getNames().contains(name) || entry.getValue() == null) {
                continue;
            }
            jsonObject.put(name, encryptOrDecryptString(isEncrypt, entry.getValue().toString(), rule));
        }
    }

    /**
     * 处理JSON对象
     *
     * @param value     JSON对象
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @return 处理后的对象
     */
    private Object encryptOrDecryptJsonObj(Object value, DbEncryptColumnRule rule, boolean isEncrypt) {
        if (CollectionUtils.isEmpty(rule.getNames()) || value == null) {
            return value;
        }
        try {
            List<Field> collect = getNotStaticField(value);
            for (Field field : collect) {
                encryptJsonObjField(value, field, rule, isEncrypt);
            }
            return value;
        } catch (IllegalAccessException e) {
            return value;
        }
    }

    /**
     * 处理JSON对象的字段
     *
     * @param value     JSON对象
     * @param field     字段
     * @param rule      加密规则
     * @param isEncrypt 是否为加密操作
     * @throws IllegalAccessException 如果访问字段失败
     */
    private void encryptJsonObjField(Object value, Field field, DbEncryptColumnRule rule, boolean isEncrypt) throws IllegalAccessException {
        String name = field.getName();
        if (!rule.getNames().contains(name)) {
            return;
        }
        fieldSetAccessible(field);
        Object v = field.get(value);
        if (v == null) {
            return;
        }
        if (!(v instanceof String)) {
            return;
        }
        fieldSet(value, field, encryptOrDecryptString(isEncrypt, (String) v, rule));
    }

    /**
     * 加解密字符串
     *
     * @param isEncrypt 是否为加密操作
     * @param v         字符串值
     * @param rule      加密规则
     * @return 处理后的字符串
     */
    private String encryptOrDecryptString(boolean isEncrypt, String v, DbEncryptColumnRule rule) {
        if (isEncrypt) {
            return EncryptionDecryptionAdapter.encryptString(v, rule, secretMap);
        }
        return EncryptionDecryptionAdapter.decryptString(v, secretMap);
    }

    /**
     * 获取集合中字符串对应的加密规则
     *
     * @param collection     集合
     * @param paramsRuleMap  参数规则映射
     * @param classAndMethod 类名.方法名
     * @return 加密规则
     * @throws RuntimeException 如果规则匹配失败
     */
    private DbEncryptColumnRule getCollectionStringRule(Collection<Object> collection, Map<String, PropertyValueBean> paramsRuleMap, String classAndMethod) throws RuntimeException {
        List<PropertyValueBean> beanList = paramsRuleMap.values().stream().filter(bean -> {
            if (!bean.isFeach() || bean.isFeachObj()) {
                return false;
            }
            return isSameValueCollection(collection, bean.getValueMap());
        }).toList();
        if (beanList.isEmpty()) {
            return null;
        }
        if (beanList.size() != 1) {
            throw new RuntimeException(classAndMethod + " 对象入参，有多个集合值相同");
        }
        PropertyValueBean bean = beanList.getFirst();
        return bean.getRule();
    }

    /**
     * 加解密字符串集合
     *
     * @param collection 字符串集合
     * @param rule       加密规则
     * @param isEncrypt  是否为加密操作
     * @param maybeJson  是否可能是JSON
     * @return 处理后的集合
     */
    private Collection<Object> encryptOrDecryptCollectionString(Collection<Object> collection, DbEncryptColumnRule rule, boolean isEncrypt, boolean maybeJson) {
        if (rule == null) {
            return collection;
        }
        Stream<String> stream = collection.stream().map(v -> maybeJson ? encryptOrDecryptJsonString((String) v, rule, isEncrypt) : encryptOrDecryptString(isEncrypt, (String) v, rule));
        if (collection instanceof Set) {
            return stream.collect(Collectors.toSet());
        } else {
            return stream.collect(Collectors.toList());
        }
    }

    /**
     * 加解密JSON对象集合
     *
     * @param collection JSON对象集合
     * @param rule       加密规则
     * @param isEncrypt  是否为加密操作
     * @return 处理后的集合
     */
    private Collection<Object> encryptOrDecryptCollectionJsonObj(Collection<Object> collection, DbEncryptColumnRule rule, boolean isEncrypt) {
        for (Object v : collection) {
            if (v instanceof JSONObject) {
                encryptOrDecryptJsonMap((JSONObject) v, rule, isEncrypt);
            } else {
                encryptOrDecryptJsonObj(v, rule, isEncrypt);
            }
        }
        return collection;
    }

    /**
     * 判断集合值是否与预期一致
     *
     * @param collection 集合
     * @param valueMap   预期值映射
     * @return 是否一致
     */
    private boolean isSameValueCollection(Collection<Object> collection, Map<Integer, Object> valueMap) {
        if (CollectionUtils.isEmpty(collection) || valueMap == null || valueMap.isEmpty() || collection.size() != valueMap.size()) {
            return false;
        }
        int index = 0;
        for (Object v : collection) {
            Object o = valueMap.get(index++);
            if (!Objects.equals(v, o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为基础类型或包装类型
     *
     * @param parameterType 类型
     * @return 是否为基础类型
     */
    protected boolean isPrimitive(Class<?> parameterType) {
        return parameterType.isPrimitive() || Number.class.isAssignableFrom(parameterType);
    }

    /**
     * 获取MetaObject
     *
     * @param mappedStatement MappedStatement
     * @param classAndMethod  类名.方法名
     * @param parameterObject 参数对象
     * @return MetaObject
     * @throws RuntimeException 如果创建失败
     */
    private MetaObject getMetaObject(MappedStatement mappedStatement, String classAndMethod, Object parameterObject) throws RuntimeException {
        Configuration configuration = mappedStatement.getConfiguration();
        if (configuration == null) {
            throw new RuntimeException(classAndMethod + " configuration为null");
        }
        return configuration.newMetaObject(parameterObject);
    }

    /**
     * 获取参数属性名
     *
     * @param index             参数索引
     * @param parameterMappings 参数映射列表
     * @param boundSql          BoundSql
     * @return 属性名
     */
    private String getProperty(Integer index, List<ParameterMapping> parameterMappings, BoundSql boundSql) {
        if (index == null || index < 0 || index >= parameterMappings.size()) {
            if (warnLogEnable()) {
                log.warn("DbEncryptionPlugin, index error, index={}, sql={}, parameterMappings={}", index, boundSql.getSql(), JSON.toJSONString(parameterMappings));
            }
            return null;
        }
        ParameterMapping parameterMapping = parameterMappings.get(index);
        if (parameterMapping == null) {
            if (warnLogEnable()) {
                log.warn("DbEncryptionPlugin, parameterMapping is null, index={}, sql={}, parameterMappings={}", index, boundSql.getSql(), JSON.toJSONString(parameterMappings));
            }
            return null;
        }
        return parameterMapping.getProperty();
    }

    /**
     * 处理非foreach参数
     *
     * @param paramRuleMap 参数规则映射
     * @param property     属性名
     * @param rule         加密规则
     */
    private void putNotFeach(Map<String, PropertyValueBean> paramRuleMap, String property, DbEncryptColumnRule rule) {
        PropertyValueBean bean = new PropertyValueBean();
        bean.setRule(rule);
        paramRuleMap.put(property, bean);
    }

    /**
     * 处理foreach对象类型参数
     *
     * @param paramRuleMap   参数规则映射
     * @param property       属性名
     * @param rule           加密规则
     * @param separatorIndex 分隔符索引
     */
    private void putFeachObj(Map<String, PropertyValueBean> paramRuleMap, String property, DbEncryptColumnRule rule, int separatorIndex) {
        String name = property.substring(separatorIndex + 1);
        PropertyValueBean bean = paramRuleMap.computeIfAbsent(name, k -> buildPropertyValueBean(rule, null));
        bean.setFeach(true);
        bean.setFeachObj(true);
    }

    /**
     * 处理foreach非对象类型参数
     *
     * @param paramRuleMap 参数规则映射
     * @param property     属性名
     * @param rule         加密规则
     * @param metaObject   MetaObject
     * @param boundSql     BoundSql
     */
    private void putFeachNotObj(Map<String, PropertyValueBean> paramRuleMap, String property, DbEncryptColumnRule rule, MetaObject metaObject, BoundSql boundSql) {
        String[] split = property.replace(FEACH, "").split("_");
        String name = split[0];
        int index = Integer.parseInt(split[1]);
        PropertyValueBean bean = paramRuleMap.computeIfAbsent(name, k -> buildPropertyValueBean(rule, new HashMap<>()));
        bean.setFeach(true);
        bean.setFeachObj(false);
        Object value = getValue(property, metaObject, boundSql);
        if (value != null) {
            bean.getValueMap().put(index, value);
        }
    }

    /**
     * 获取参数值
     *
     * @param property   属性名
     * @param metaObject MetaObject
     * @param boundSql   BoundSql
     * @return 属性值
     */
    private Object getValue(String property, MetaObject metaObject, BoundSql boundSql) {
        if (metaObject.hasGetter(property)) {
            return metaObject.getValue(property);
        } else if (boundSql.hasAdditionalParameter(property)) {
            return boundSql.getAdditionalParameter(property);
        } else {
            return null;
        }
    }

    /**
     * 构建PropertyValueBean
     *
     * @param rule     加密规则
     * @param valueMap 值映射
     * @return PropertyValueBean
     */
    private PropertyValueBean buildPropertyValueBean(DbEncryptColumnRule rule, Map<Integer, Object> valueMap) {
        PropertyValueBean bean = new PropertyValueBean();
        bean.setRule(rule);
        bean.setValueMap(valueMap);
        return bean;
    }

    /**
     * 转换为Map
     *
     * @param value 值
     * @return Map
     */
    private Map<Object, Object> toMap(Object value) {
        return (Map<Object, Object>) value;
    }

    /**
     * 转换为Collection
     *
     * @param value 值
     * @return Collection
     */
    private Collection<Object> toCollection(Object value) {
        return (Collection<Object>) value;
    }

    /**
     * 设置字段可访问
     *
     * @param field 字段
     */
    private void fieldSetAccessible(Field field) {
        field.setAccessible(true);
    }

    /**
     * 设置字段值
     *
     * @param obj   对象
     * @param field 字段
     * @param value 值
     * @throws IllegalAccessException 如果访问字段失败
     */
    private void fieldSet(Object obj, Field field, Object value) throws IllegalAccessException {
        field.set(obj, value);
    }

    /**
     * 用于包装目标对象，添加拦截功能
     *
     * @param target 目标对象
     * @return 包装后的对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置配置属性
     *
     * @param properties 配置属性
     */
    @Override
    public void setProperties(Properties properties) {
        putTables(properties);
        putSecretMap(properties);
        putLogLevel(properties);
    }

    /**
     * 设置日志级别
     *
     * @param properties 配置属性
     */
    private void putLogLevel(Properties properties) {
        this.logLevel = properties.getProperty("logLevel");
    }

    /**
     * 设置密钥映射
     *
     * @param properties 配置属性
     */
    private void putSecretMap(Properties properties) {
        String secretMapJson = properties.getProperty("secretMap");
        if (secretMapJson == null || secretMapJson.isEmpty()) {
            return;
        }
        try {
            Map<String, String> propertiesSecretMap = JSON.parseObject(secretMapJson, new TypeReference<Map<String, String>>() {
            });
            if (propertiesSecretMap == null || propertiesSecretMap.isEmpty()) {
                return;
            }
            secretMap.putAll(propertiesSecretMap);
        } catch (Exception e) {
            if (errorLogEnable()) {
                log.error("DbEncryptionPlugin, add secretMap error, e:", e);
            }
        }
    }

    /**
     * 设置表配置
     *
     * @param properties 配置属性
     */
    private void putTables(Properties properties) {
        String tablesJson = properties.getProperty("tables");
        if (tablesJson == null || tablesJson.isEmpty()) {
            return;
        }
        try {
            Map<String, DbEncryptTableRule> propertiesTables = JSON.parseObject(tablesJson, new TypeReference<Map<String, DbEncryptTableRule>>() {
            });
            if (propertiesTables == null || propertiesTables.isEmpty()) {
                return;
            }
            tables.putAll(propertiesTables);
        } catch (Exception e) {
            if (errorLogEnable()) {
                log.error("DbEncryptionPlugin, add tables error, e:", e);
            }
        }
    }

    /**
     * 判断error日志是否启用
     *
     * @return 是否启用
     */
    private boolean errorLogEnable() {
        return "error".equals(this.logLevel);
    }

    /**
     * 判断warn日志是否启用
     *
     * @return 是否启用
     */
    private boolean warnLogEnable() {
        return "warn".equals(this.logLevel);
    }

    /**
     * 判断info日志是否启用
     *
     * @return 是否启用
     */
    private boolean infoLogEnable() {
        return "info".equals(this.logLevel);
    }
}