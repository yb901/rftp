package com.zy.common.core.dbencrypt.config;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据库表加密规则配置类
 *
 * @author zzy
 * @date 2026/05/04
 */
@Data
public class DbEncryptTableRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表的列名与加密规则的映射
     */
    private Map<String, DbEncryptColumnRule> columns = new LinkedHashMap<>();
}
