package com.zy.common.utils;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.lang.Console;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * ID编码工具类 - 基于SM4-CTR的ID加密与HMAC完整性保护
 * 编码格式：Base62(SM4-CTR(id[8字节] + mac[2字节]))
 * 输出固定长度：14 字符
 * 密钥配置：环境变量/系统属性中的密钥采用 Base62 编码
 */
@Slf4j
public final class IdEncodeUtil {
    /**
     * Base62 输出固定长度（10字节密文的理论最大Base62长度为14）
     */
    private static final int FIXED_BASE62_LEN = 14;

    private static final byte[] SM4_KEY;
    private static final byte[] HMAC_KEY;
    private static final Sm4Ctr SM4_CTR;
    private static final byte[] FIXED_IV = new byte[16];

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//        // 使用 Base62 解码密钥（密钥需预先用 Base62 编码）
//        String idKey = generateRandomKey();
//        String hmacKey = generateRandomKey();
//        Console.log("idKey="+idKey);
//        Console.log("hmacKey="+hmacKey);
        SM4_KEY = loadKeyBase62("SM4_ID_KEY", "sm4.id.key", "48ed8bhYKCEi9vAlTyDXD4");
        HMAC_KEY = loadKeyBase62("SM4_HMAC_KEY", "sm4.hmac.key", "6GRWMgZkFnLBS3uSw9cs3j");
        SM4_CTR = new Sm4Ctr(SM4_KEY, FIXED_IV);
    }

    /**
     * 从环境变量或系统属性加载 Base62 编码的密钥
     */
    private static byte[] loadKeyBase62(String envName, String propName, String defaultKeyRaw) {
        String keyBase62 = System.getenv(envName);
        if (keyBase62 == null || keyBase62.isEmpty()) {
            keyBase62 = System.getProperty(propName);
        }
        if (keyBase62 == null || keyBase62.isEmpty()) {
            keyBase62 = defaultKeyRaw;
        }

        byte[] decoded = Base62.decode(keyBase62);
        // 统一补齐到16字节（左边补0）
        if (decoded.length == 16) {
            return decoded;
        }
        byte[] padded = new byte[16];
        if (decoded.length < 16) {
            System.arraycopy(decoded, 0, padded, 16 - decoded.length, decoded.length);
        } else {
            // 如果超过16字节（理论上不可能，但防御），截取前16字节
            System.arraycopy(decoded, 0, padded, 0, 16);
        }
        return padded;
    }

    public static String encodeId(Long value) {
        if (value == null) return null;
        try {
            byte[] idBytes = longToBytes(value);
            byte[] mac = computeMac(idBytes);
            byte[] plain = new byte[10];
            System.arraycopy(idBytes, 0, plain, 0, 8);
            System.arraycopy(mac, 0, plain, 8, 2);
            byte[] cipher = SM4_CTR.encrypt(plain);
            return toFixedBase62(cipher);
        } catch (Exception e) {
            log.error("encodeId error, value={}", value, e);
            return null;
        }
    }

    public static Long decodeId(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            byte[] cipher = fromFixedBase62(data);
            byte[] plain = SM4_CTR.decrypt(cipher);
            byte[] idBytes = new byte[8];
            byte[] mac = new byte[2];
            System.arraycopy(plain, 0, idBytes, 0, 8);
            System.arraycopy(plain, 8, mac, 0, 2);
            byte[] expectedMac = computeMac(idBytes);
            if (!Arrays.equals(mac, expectedMac)) {
                log.warn("MAC verification failed for data: {}", data);
                return null;
            }
            return bytesToLong(idBytes);
        } catch (Exception e) {
            log.error("decodeId error, data={}", data, e);
            return null;
        }
    }

    private static byte[] computeMac(byte[] data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(HMAC_KEY, "HmacSHA256"));
            byte[] fullMac = hmac.doFinal(data);
            return new byte[]{fullMac[0], fullMac[1]};
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return bytes;
    }

    private static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[i] & 0xff);
        }
        return value;
    }

    /**
     * Base62 编码 + 固定长度补零
     */
    private static String toFixedBase62(byte[] data) {
        String raw = Base62.encode(data);
        return String.format("%" + FIXED_BASE62_LEN + "s", raw).replace(' ', '0');
    }

    /**
     * Base62 解码（固定长度）
     */
    private static byte[] fromFixedBase62(String str) {
        if (str.length() != FIXED_BASE62_LEN) {
            throw new IllegalArgumentException("Invalid Base62 length, expected " + FIXED_BASE62_LEN);
        }
        String trimmed = str.replaceFirst("^0+", "");
        if (trimmed.isEmpty()) trimmed = "0";
        byte[] bytes = Base62.decode(trimmed);
        if (bytes.length < 10) {
            byte[] padded = new byte[10];
            System.arraycopy(bytes, 0, padded, 10 - bytes.length, bytes.length);
            return padded;
        } else if (bytes.length > 10) {
            byte[] trimmedBytes = new byte[10];
            System.arraycopy(bytes, 0, trimmedBytes, 0, 10);
            return trimmedBytes;
        }
        return bytes;
    }

    private static class Sm4Ctr {
        private static final String ALGORITHM = "SM4/CTR/NoPadding";
        private final SecretKeySpec keySpec;
        private final IvParameterSpec ivSpec;

        Sm4Ctr(byte[] key, byte[] iv) {
            if (key.length != 16 || iv.length != 16)
                throw new IllegalArgumentException("Key/IV must be 16 bytes");
            this.keySpec = new SecretKeySpec(key, "SM4");
            this.ivSpec = new IvParameterSpec(iv);
        }

        byte[] encrypt(byte[] plaintext) {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                return cipher.doFinal(plaintext);
            } catch (Exception e) {
                throw new RuntimeException("SM4-CTR encrypt failed", e);
            }
        }

        byte[] decrypt(byte[] ciphertext) {
            return encrypt(ciphertext);
        }
    }


    private static String generateRandomKey() {
        SecureRandom sr = new SecureRandom();
        // 始终补足16字节
        byte[] key = new byte[16];
        sr.nextBytes(key);
        String encoded = Base62.encode(key);
        byte[] decoded = Base62.decode(encoded);
        if (decoded.length < 16) {
            // 补足前导零
            byte[] padded = new byte[16];
            System.arraycopy(decoded, 0, padded, 16 - decoded.length, decoded.length);
            return Base62.encode(padded); // 重新编码确保长度固定
        }
        return encoded;
    }

    // 测试
    public static void main(String[] args) {
        long id = Long.MAX_VALUE;
        String enc = encodeId(id);
        Console.log("Encoded: {} (len={})", enc, enc.length());   // 长度固定为14
        Console.log("Decoded: {}", decodeId(enc));

        long id22 = Long.MIN_VALUE;
        String enc22 = encodeId(id22);
        Console.log("Encoded : {} (len={})", enc22, enc22.length());   // 长度固定为14
        Console.log("Decoded: {}", decodeId(enc22));

        for (int i = 1000000; i < 10001000; i++) {
            Console.log("{} -> {}", i, encodeId((long) i));
        }

        //    Console.log("Decoded: {}",  decodeId("2UX1JQrGbfalE8"));
    }
}