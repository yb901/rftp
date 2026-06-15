package com.zy.common.core.dbencrypt.handler.encrypt.impl;

import com.zy.common.core.dbencrypt.enums.EncryptorEnum;
import com.zy.common.core.dbencrypt.handler.encrypt.EncryptionDecryptionAdapter;
import com.zy.common.core.dbencrypt.handler.encrypt.EncryptionDecryptionHandler;
import com.zy.common.utils.Sm4Util;
import org.springframework.stereotype.Service;

/**
 * SM4加解密处理器
 * <p>
 * 实现基于SM4算法的字符串加密和解密功能
 *
 * @author zzy
 * @date 2026/05/04
 */
@Service
public class Sm4Handler implements EncryptionDecryptionHandler {

    /**
     * 获取加密方式
     *
     * @return SM4加密方式枚举
     */
    @Override
    public EncryptorEnum encryptorEnum() {
        return EncryptorEnum.S;
    }

    /**
     * 使用SM4算法加密字符串
     *
     * @param value  原始字符串
     * @param secret 密钥
     * @return 加密后的字符串
     */
    @Override
    public String encrypt(String value, String secret) {
        return Sm4Util.encrypt(value, secret);
    }

    /**
     * 使用SM4算法解密字符串
     *
     * @param value  加密字符串
     * @param secret 密钥
     * @return 解密后的原始字符串
     */
    @Override
    public String decrypt(String value, String secret) {
        return Sm4Util.decrypt(value, secret);
    }

    /**
     * 注册当前处理器到EncryptionDecryptionAdapter
     */
    @Override
    public void afterPropertiesSet() {
        EncryptionDecryptionAdapter.register(this);
    }
}