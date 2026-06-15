package com.zy.common.core.dbencrypt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 加密方式枚举
 *
 * @author zzy
 * @date 2026/05/04
 */
@Getter
@AllArgsConstructor
public enum EncryptorEnum {

    /**
     * SM4加密方式
     */
    S("SM4"),
    ;

    /**
     * 加密方式描述
     */
    private final String desc;
}
