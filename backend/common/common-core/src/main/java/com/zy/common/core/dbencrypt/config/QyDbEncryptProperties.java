package com.zy.common.core.dbencrypt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据库加解密配置属性类
 *
 * @author zzy
 * @date 2026/05/04
 */
@Getter
@Setter
@ConfigurationProperties(prefix = QyDbEncryptProperties.QUAN_YI_ENCRYPT_PREFIX)
public class QyDbEncryptProperties {

    /**
     * 配置前缀
     */
    public static final String QUAN_YI_ENCRYPT_PREFIX = "db.encrypt";

    /**
     * 数据库表相关配置
     * <p>
     * 配置示例：
     * db.encrypt.tables.${tableName}.columns.${columnName}.encryptor=A
     * db.encrypt.tables.${tableName}.columns.${columnName}.version=1
     * db.encrypt.tables.${tableName}.columns.${columnName}.names=userPhone,idCard
     * </p>
     */
    private Map<String, DbEncryptTableRule> tables = new LinkedHashMap<>();

    /**
     * 密钥相关配置
     * <p>
     * 配置示例：
     * db.encrypt.secrets.S1=123456
     * db.encrypt.secrets.S2=123456
     * </p>
     */
    private Map<String, String> secrets = new LinkedHashMap<>();

    /**
     * 日志级别：close/error/info/debug
     */
    private String logLevel = "error";
}
