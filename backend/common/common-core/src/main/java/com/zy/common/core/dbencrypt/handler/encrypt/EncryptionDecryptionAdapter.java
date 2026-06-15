package com.zy.common.core.dbencrypt.handler.encrypt;

import com.zy.common.core.dbencrypt.bean.DbEncryptionConstant;
import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * 加解密适配器
 * <p>
 * 负责管理和分发不同加密方式的加解密逻辑
 *
 * @author zzy
 * @date 2026/05/04
 */
@Slf4j
public class EncryptionDecryptionAdapter {

    /**
     * 加密方式与处理器的映射
     */
    private static final Map<String, EncryptionDecryptionHandler> handlerMap = new HashMap<>();

    private EncryptionDecryptionAdapter() {
    }

    /**
     * 注册加解密处理器
     *
     * @param handler 加解密处理器
     */
    public static void register(EncryptionDecryptionHandler handler) {
        handlerMap.put(handler.encryptorEnum().name(), handler);
    }

    /**
     * 根据加密方式获取对应的处理器
     *
     * @param encryptor 加密方式
     * @return 加解密处理器
     */
    private static EncryptionDecryptionHandler get(String encryptor) {
        EncryptionDecryptionHandler handler = handlerMap.get(encryptor);
        if (handler == null) {
            log.info("EncryptionDecryption, handler is null, encryptor={}", encryptor);
            return null;
        }
        return handler;
    }

    /**
     * 加密字符串
     *
     * @param value     原始字符串
     * @param rule      加密规则
     * @param secretMap 密钥映射：key为"加密方式+版本号"，value为密钥
     * @return 加密后的字符串
     */
    public static String encryptString(String value, DbEncryptColumnRule rule, Map<String, String> secretMap) {
        if (StringUtils.isBlank(value) || rule == null || MapUtils.isEmpty(secretMap)) {
            return value;
        }
        if (BooleanUtils.isNotTrue(rule.getStart())) {
            return value;
        }
        EncryptionDecryptionHandler handler = get(rule.getEncryptor());
        if (handler == null) {
            return value;
        }
        try {
            Pair<String, Integer> pair = parseEncryptorVersion(value);
            if (pair.getLeft() != null && pair.getRight() != null && get(pair.getLeft()) != null) {
                // 已经是密文
                return value;
            }
            String secretKey = getSecretKey(rule.getEncryptor(), rule.getVersion());
            String secret = secretMap.get(secretKey);
            if (StringUtils.isBlank(secret)) {
                log.warn("EncryptionDecryption, encrypt secret is blank, encryptor={}, version={}", rule.getEncryptor(), rule.getVersion());
                return value;
            }
            String encrypt = handler.encrypt(value, secret);
            if (StringUtils.isBlank(encrypt)) {
                return value;
            }
            return secretKey + DbEncryptionConstant.UNDERLINE + encrypt;
        } catch (Exception e) {
            log.error("EncryptionDecryption, encrypt error, encryptor={}, version={}", rule.getEncryptor(), rule.getVersion());
            return value;
        }
    }

    /**
     * 解析密文中的加密方式和版本号
     *
     * @param value 密文字符串
     * @return 加密方式和版本号的配对
     */
    private static Pair<String, Integer> parseEncryptorVersion(String value) {
        try {
            int index = value.indexOf(DbEncryptionConstant.UNDERLINE);
            if (index <= 0) {
                return Pair.of(null, null);
            }
            String encryptor = value.substring(0, 1);
            Integer version = Integer.parseInt(value.substring(1, index));
            return Pair.of(encryptor, version);
        } catch (Exception e) {
            return Pair.of(null, null);
        }
    }

    /**
     * 解密字符串
     *
     * @param value     加密字符串
     * @param secretMap 密钥映射：key为"加密方式+版本号"，value为密钥
     * @return 解密后的原始字符串
     */
    public static String decryptString(String value, Map<String, String> secretMap) {
        if (StringUtils.isBlank(value) || MapUtils.isEmpty(secretMap)) {
            return value;
        }
        Pair<String, Integer> pair = parseEncryptorVersion(value);
        if (StringUtils.isBlank(pair.getLeft()) || pair.getRight() == null) {
            return value;
        }
        EncryptionDecryptionHandler handler = get(pair.getLeft());
        if (handler == null) {
            return value;
        }
        try {
            String secretKey = getSecretKey(pair.getLeft(), pair.getRight());
            String secret = secretMap.get(secretKey);
            if (StringUtils.isBlank(secret)) {
                log.warn("EncryptionDecryption, decrypt secret is blank, encryptor={}, version={}", pair.getLeft(), pair.getRight());
                return value;
            }
            String decrypt = handler.decrypt(value.substring((secretKey + DbEncryptionConstant.UNDERLINE).length()), secret);
            if (StringUtils.isBlank(decrypt)) {
                return value;
            }
            return decrypt;
        } catch (Exception e) {
            log.error("EncryptionDecryption, decrypt error, encryptor={}, version={}", pair.getLeft(), pair.getRight());
            return value;
        }
    }

    /**
     * 构建密钥标识
     *
     * @param encryptor 加密方式
     * @param version   版本号
     * @return 密钥标识，格式：加密方式+版本号
     */
    private static String getSecretKey(String encryptor, Integer version) {
        return encryptor + version;
    }
}