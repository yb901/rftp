package com.zy.common.core.dbencrypt.config;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 数据库列加密规则配置类
 *
 * @author zzy
 * @date 2026/05/04
 */
@Data
public class DbEncryptColumnRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 加密方式
     */
    private String encryptor;

    /**
     * 加密版本号
     */
    private Integer version;

    /**
     * 是否开启加密
     */
    private Boolean start;

    /**
     * JSON中需要加密的字段名列表
     */
    private List<String> names;
}
