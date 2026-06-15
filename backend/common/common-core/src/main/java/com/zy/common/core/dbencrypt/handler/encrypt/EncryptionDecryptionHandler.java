package com.zy.common.core.dbencrypt.handler.encrypt;

import com.zy.common.core.dbencrypt.enums.EncryptorEnum;
import org.springframework.beans.factory.InitializingBean;

/**
 * 加解密处理器接口
 * <p>
 * 定义加解密的通用操作规范，具体加密算法实现此接口
 *
 * @author zzy
 * @date 2026/05/04
 */
public interface EncryptionDecryptionHandler extends InitializingBean {

    /**
     * 获取加密方式
     *
     * @return 加密方式枚举
     */
    EncryptorEnum encryptorEnum();

    /**
     * 加密字符串
     *
     * @param value  原始字符串
     * @param secret 密钥
     * @return 加密后的字符串
     */
    String encrypt(String value, String secret);

    /**
     * 解密字符串
     *
     * @param value  加密字符串
     * @param secret 密钥
     * @return 解密后的原始字符串
     */
    String decrypt(String value, String secret);
}