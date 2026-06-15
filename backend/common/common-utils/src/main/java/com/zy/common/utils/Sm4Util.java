package com.zy.common.utils;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.util.RandomUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * SM4对称加密工具类（CBC模式，随机IV，Base62编码）
 * <p>
 * 加密结果格式：Base62( IV + 密文 )
 * </p>
 */
public final class Sm4Util {

    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS5Padding";
    private static final int KEY_LEN = 16;      // 128位密钥
    private static final int IV_LEN = 16;       // CBC IV长度

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }


    /**
     * SM4加密
     *
     * @param plaintext 明文（UTF-8）
     * @param key       16字节密钥
     * @return Base62编码的密文（IV+密文）
     */
    public static String encrypt(String plaintext, String key) {
        return encrypt(plaintext, Base62.decode(key));
    }

    /**
     * SM4加密
     *
     * @param plaintext 明文（UTF-8）
     * @param key       16字节密钥
     * @return Base62编码的密文（IV+密文）
     */
    private static String encrypt(String plaintext, byte[] key) {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("明文不能为空");
        }
        validateKey(key);

        try {
            // 生成随机IV
            byte[] iv = RandomUtil.randomBytes(IV_LEN);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 拼接 IV + 密文
            byte[] result = new byte[IV_LEN + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, IV_LEN);
            System.arraycopy(ciphertext, 0, result, IV_LEN, ciphertext.length);

            return Base62.encode(result);
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * SM4解密
     *
     * @param ciphertextBase62
     * @param key
     * @return
     */
    public static String decrypt(String ciphertextBase62, String key) {
        return decrypt(ciphertextBase62, Base62.decode(key));
    }

    /**
     * SM4解密
     *
     * @param ciphertextBase62 Base62编码的密文（包含IV）
     * @param key              16字节密钥
     * @return 明文
     */
    private static String decrypt(String ciphertextBase62, byte[] key) {
        if (ciphertextBase62 == null || ciphertextBase62.isEmpty()) {
            throw new IllegalArgumentException("密文不能为空");
        }
        validateKey(key);

        try {
            byte[] data = Base62.decode(ciphertextBase62);
            if (data.length < IV_LEN) {
                throw new IllegalArgumentException("密文数据太短，无法提取IV");
            }

            // 提取IV和密文
            byte[] iv = new byte[IV_LEN];
            byte[] ciphertext = new byte[data.length - IV_LEN];
            System.arraycopy(data, 0, iv, 0, IV_LEN);
            System.arraycopy(data, IV_LEN, ciphertext, 0, ciphertext.length);

            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成随机16字节密钥
     */
    public static byte[] generateKey() {
        return RandomUtil.randomBytes(KEY_LEN);
    }

    /**
     * 校验密钥长度
     */
    private static void validateKey(byte[] key) {
        if (key == null || key.length != KEY_LEN) {
            throw new IllegalArgumentException("密钥必须是16字节的byte数组");
        }
    }

    /**
     * 字节数组转十六进制字符串（仅供测试输出用）
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 命令行入口。
     *
     * @param args 命令参数
     */
    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            runCommand(args);
            return;
        }
        try {
            // 1. 生成随机密钥（实际使用时请安全保存）
            byte[] key = generateKey();
            System.out.println("密钥Base62: " + Base62.encode(key));

            // 2. 原始明文
            String originalText = "Hello SM4 + Base62 + CBC！";
            System.out.println("\n原始明文: " + originalText);

            // 3. 加密
            String encrypted = encrypt(originalText, key);
            System.out.println("加密结果(Base62): " + encrypted);

            // 4. 解密
            String decrypted = decrypt(encrypted, key);
            System.out.println("解密结果: " + decrypted);

            // 5. 验证一致性
            if (originalText.equals(decrypted)) {
                System.out.println("\n✅ 测试通过：加密解密一致");
            } else {
                System.err.println("\n❌ 测试失败：解密结果与原文不符");
            }

            // 6. 额外测试：相同明文每次密文不同（由于随机IV）
            String encrypted2 = encrypt(originalText, key);
            System.out.println("\n第二次加密结果: " + encrypted2);
            if (!encrypted.equals(encrypted2)) {
                System.out.println("✅ 随机IV生效：两次密文不同");
            } else {
                System.out.println("⚠️ 两次密文相同（IV未随机化）");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行命令行加解密动作。
     *
     * @param args 命令参数
     */
    private static void runCommand(String[] args) {
        String command = args[0];
        if ("generate-key".equals(command)) {
            System.out.println(Base62.encode(generateKey()));
            return;
        }
        if ("encrypt".equals(command) && args.length >= 3) {
            System.out.println(encrypt(args[2], args[1]));
            return;
        }
        if ("decrypt".equals(command) && args.length >= 3) {
            System.out.println(decrypt(args[2], args[1]));
            return;
        }
        System.err.println("用法:");
        System.err.println("  generate-key");
        System.err.println("  encrypt <Base62密钥> <明文>");
        System.err.println("  decrypt <Base62密钥> <密文>");
    }

}
