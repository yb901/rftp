package com.zy.common.utils;

import cn.hutool.core.codec.Base62;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * SM2 非对称加密工具类
 * <p>
 * 功能：生成密钥对、签名/验签、加密/解密。
 * 编码：密钥、密文、签名均采用 Base62 编码。
 * </p>
 */
public final class Sm2Util {

    private static final String PROVIDER_NAME = "BC";
    private static final String KEY_ALGORITHM = "EC";           // KeyPairGenerator 使用 EC
    private static final String SM2_CURVE_NAME = "sm2p256v1";   // SM2 推荐曲线
    private static final String CIPHER_ALGORITHM = "SM2";       // 加密算法
    private static final String SIGN_ALGORITHM = "SM3withSM2";  // 签名算法

    static {
        if (Security.getProvider(PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // ==================== 1. 密钥对生成 ====================

    /**
     * 生成 SM2 密钥对
     *
     * @return KeyPair 对象
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER_NAME);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(SM2_CURVE_NAME);
            keyPairGenerator.initialize(ecSpec, new SecureRandom());
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("生成 SM2 密钥对失败", e);
        }
    }

    /**
     * 获取 Base62 编码的公钥（X.509 格式）
     */
    public static String getPublicKeyBase62(PublicKey publicKey) {
        return Base62.encode(publicKey.getEncoded());
    }

    /**
     * 获取 Base62 编码的私钥（PKCS#8 格式）
     */
    public static String getPrivateKeyBase62(PrivateKey privateKey) {
        return Base62.encode(privateKey.getEncoded());
    }

    /**
     * 从 Base62 编码的公钥字符串恢复 PublicKey 对象
     */
    public static PublicKey restorePublicKey(String publicKeyBase62) {
        try {
            byte[] keyBytes = Base62.decode(publicKeyBase62);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER_NAME);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("恢复公钥失败", e);
        }
    }

    /**
     * 从 Base62 编码的私钥字符串恢复 PrivateKey 对象
     */
    public static PrivateKey restorePrivateKey(String privateKeyBase62) {
        try {
            byte[] keyBytes = Base62.decode(privateKeyBase62);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER_NAME);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("恢复私钥失败", e);
        }
    }

    // ==================== 2. 签名 & 验签 ====================

    /**
     * 使用私钥对消息进行签名
     *
     * @param data       待签名的原始数据（字节数组）
     * @param privateKey 私钥
     * @return Base62 编码的签名值
     */
    public static String sign(byte[] data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM, PROVIDER_NAME);
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signBytes = signature.sign();
            return Base62.encode(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM2 签名失败", e);
        }
    }

    /**
     * 使用公钥验证签名
     *
     * @param data       原始数据
     * @param signBase62 Base62 编码的签名值
     * @param publicKey  公钥
     * @return true 验证通过，false 验证失败
     */
    public static boolean verify(byte[] data, String signBase62, PublicKey publicKey) {
        try {
            byte[] signBytes = Base62.decode(signBase62);
            Signature signature = Signature.getInstance(SIGN_ALGORITHM, PROVIDER_NAME);
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM2 验签失败", e);
        }
    }

    // ==================== 3. 加密 & 解密 ====================

    /**
     * 使用公钥加密数据
     *
     * @param data      明文数据
     * @param publicKey 公钥
     * @return Base62 编码的密文
     */
    public static String encrypt(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherBytes = cipher.doFinal(data);
            return Base62.encode(cipherBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM2 加密失败", e);
        }
    }

    /**
     * 使用私钥解密密文
     *
     * @param ciphertextBase62 Base62 编码的密文
     * @param privateKey       私钥
     * @return 明文数据
     */
    public static byte[] decrypt(String ciphertextBase62, PrivateKey privateKey) {
        try {
            byte[] cipherBytes = Base62.decode(ciphertextBase62);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(cipherBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM2 解密失败", e);
        }
    }

    // ==================== 便利方法（字符串 UTF-8） ====================

    public static String sign(String message, PrivateKey privateKey) {
        return sign(message.getBytes(java.nio.charset.StandardCharsets.UTF_8), privateKey);
    }

    public static boolean verify(String message, String signBase62, PublicKey publicKey) {
        return verify(message.getBytes(java.nio.charset.StandardCharsets.UTF_8), signBase62, publicKey);
    }

    public static String encrypt(String plainText, PublicKey publicKey) {
        return encrypt(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8), publicKey);
    }

    public static String decryptToString(String ciphertextBase62, PrivateKey privateKey) {
        byte[] plainBytes = decrypt(ciphertextBase62, privateKey);
        return new String(plainBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    // ==================== 测试入口 ====================
    public static void main(String[] args) {
        try {
            // 1. 生成密钥对
            KeyPair keyPair = generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String pubBase62 = getPublicKeyBase62(publicKey);
            String priBase62 = getPrivateKeyBase62(privateKey);
            System.out.println("公钥(Base62): " + pubBase62);
            System.out.println("私钥(Base62): " + priBase62);

            // 2. 签名 & 验签
            String message = "SM2 签名测试消息 Hello World! 中文支持。";
            System.out.println("\n原始消息: " + message);

            String signature = sign(message, privateKey);
            System.out.println("签名(Base62): " + signature);

            boolean verifyResult = verify(message, signature, publicKey);
            System.out.println("验签结果: " + (verifyResult ? "✅ 通过" : "❌ 失败"));

            // 3. 篡改消息验签（测试失败情况）
            String tamperedMessage = message + "被篡改";
            boolean tamperedVerify = verify(tamperedMessage, signature, publicKey);
            System.out.println("篡改消息验签: " + (tamperedVerify ? "❌ 意外通过" : "✅ 正确拒绝"));

            // 4. 加密 & 解密
            String plainText = "SM2 加密测试数据：重要机密信息 123456!@#";
            System.out.println("\n待加密明文: " + plainText);

            String cipherText = encrypt(plainText, publicKey);
            System.out.println("加密密文(Base62): " + cipherText);

            String decryptedText = decryptToString(cipherText, privateKey);
            System.out.println("解密结果: " + decryptedText);

            // 5. 加解密一致性校验
            if (plainText.equals(decryptedText)) {
                System.out.println("\n✅ 加密解密一致性测试通过");
            } else {
                System.out.println("\n❌ 加密解密一致性测试失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}