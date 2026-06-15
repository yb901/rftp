package com.zy.common.utils;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.util.RandomUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.time.Instant;

/**
 * Cookie 安全加密工具类（SM4-GCM + Base62）
 *
 * <p>采用国密算法 SM4 的 GCM 认证加密模式，同时完成加密和认证。
 *
 * <p>输出格式：时间戳_Base62(nonce + 密文)_Base62(认证标签)
 * <ul>
 *   <li>时间戳：明文，用于服务端快速判断过期，同时作为附加认证数据（AAD）</li>
 *   <li>密文部分：SM4-GCM 加密后的敏感数据，拼接 Nonce 后用 Base62 编码</li>
 *   <li>认证标签：SM4-GCM 自动生成的 16 字节 Tag，用 Base62 编码，用于完整性校验</li>
 * </ul>
 *
 * <p>特性：
 * <ul>
 *   <li>一次加密 + 认证：GCM 模式同时完成加密和认证，无需额外 HMAC</li>
 *   <li>防篡改：任何对 Cookie 内容（包括时间戳）的修改都会导致解密失败</li>
 *   <li>防重放：服务端校验时间戳有效期（可配置）</li>
 *   <li>长度紧凑：典型 50 字节明文 → 最终 Cookie 约 120~150 字符</li>
 *   <li>密钥外部化：密钥作为方法参数传入，便于集成 KMS</li>
 * </ul>
 *
 * <p>注意：密钥必须持久化固定，禁止在每次加密时生成新密钥，否则已下发的 Cookie 无法解密。
 *
 * @author developer
 * @see <a href="backend/sm-crypto-readme.md">SM4-GCM Cookie 加密使用指南</a>
 */
public class Sm4GcmCookie {

    /**
     * 算法：SM4-GCM 模式，NoPadding
     */
    private static final String ALGORITHM = "SM4/GCM/NoPadding";

    /**
     * Nonce 长度：12 字节（SM4-GCM 推荐值）
     */
    private static final int NONCE_LEN = 12;

    /**
     * GCM 认证标签长度：128 位（16 字节）
     */
    private static final int GCM_TAG_LEN = 128;

    /**
     * 安全随机数生成器
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        // 注册 BouncyCastle 作为安全提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密 Cookie
     *
     * @param plainText 敏感信息（UTF-8 编码的明文）
     * @param secretKey Base62 编码的 SM4 密钥（16 字节）
     * @return 加密后的 Cookie 值，格式：时间戳_Base62(nonce+密文)_Base62(tag)
     * @throws Exception 加密失败时抛出（密钥无效等）
     */
    public static String encrypt(String plainText, String secretKey) throws Exception {
        // 1. 生成时间戳（Unix 秒级时间戳），作为附加认证数据（AAD）
        long timestamp = Instant.now().getEpochSecond();
        String timestampStr = Long.toString(timestamp);

        // 2. 生成随机 Nonce（12 字节），每次加密都会生成不同的随机数
        byte[] nonce = new byte[NONCE_LEN];
        RANDOM.nextBytes(nonce);

        // 3. SM4-GCM 加密
        SecretKeySpec keySpec = new SecretKeySpec(Base62.decode(secretKey), "SM4");
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LEN, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        // 将时间戳绑定为附加数据，防止时间戳被单独篡改
        cipher.updateAAD(timestampStr.getBytes(StandardCharsets.UTF_8));

        // 执行加密，结果包含密文和认证标签（Tag）
        byte[] ciphertextWithTag = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 4. 分离密文和认证标签
        int cipherLen = ciphertextWithTag.length - GCM_TAG_LEN / 8;
        byte[] ciphertextOnly = new byte[cipherLen];
        byte[] tag = new byte[GCM_TAG_LEN / 8];
        System.arraycopy(ciphertextWithTag, 0, ciphertextOnly, 0, cipherLen);
        System.arraycopy(ciphertextWithTag, cipherLen, tag, 0, tag.length);

        // 5. 拼接 Nonce 和密文，然后 Base62 编码
        byte[] nonceAndCipher = new byte[nonce.length + ciphertextOnly.length];
        System.arraycopy(nonce, 0, nonceAndCipher, 0, nonce.length);
        System.arraycopy(ciphertextOnly, 0, nonceAndCipher, nonce.length, ciphertextOnly.length);

        String encodedData = Base62.encode(nonceAndCipher);
        String encodedTag = Base62.encode(tag);

        // 6. 返回最终格式：时间戳_Base62编码的数据_Base62编码的标签
        return timestampStr + "_" + encodedData + "_" + encodedTag;
    }

    /**
     * 解密并验证 Cookie
     *
     * @param cookieValue   加密后的 Cookie 值
     * @param secretKey     Base62 编码的 SM4 密钥（16 字节），必须与加密时使用的密钥相同
     * @param maxAgeSeconds 允许的最大有效期（秒），用于防重放
     * @return 解密后的原始数据（UTF-8 编码）
     * @throws IllegalArgumentException 格式错误
     * @throws SecurityException        过期或篡改
     * @throws Exception                解密失败（密钥错误等）
     */
    public static String decrypt(String cookieValue, String secretKey, long maxAgeSeconds) throws Exception {
        // 1. 解析 Cookie 格式：时间戳_Base62数据_Base62标签
        String[] parts = cookieValue.split("_", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid cookie format");
        }

        String timestampStr = parts[0];
        String encodedData = parts[1];
        String encodedTag = parts[2];

        // 2. 验证时间戳有效期（防重放攻击）
        long timestamp = Long.parseLong(timestampStr);
        long now = Instant.now().getEpochSecond();

        // 检查是否过期
        if (now - timestamp > maxAgeSeconds) {
            throw new SecurityException("Cookie expired");
        }
        // 防止未来时间戳攻击（允许 5 分钟时钟偏差）
        if (timestamp - now > 300) {
            throw new SecurityException("Timestamp in future");
        }

        // 3. Base62 解码
        byte[] nonceAndCipher = Base62.decode(encodedData);
        byte[] tag = Base62.decode(encodedTag);

        if (nonceAndCipher.length < NONCE_LEN) {
            throw new SecurityException("Invalid data length");
        }

        // 4. 分离 Nonce 和密文
        byte[] nonce = new byte[NONCE_LEN];
        byte[] ciphertextOnly = new byte[nonceAndCipher.length - NONCE_LEN];
        System.arraycopy(nonceAndCipher, 0, nonce, 0, NONCE_LEN);
        System.arraycopy(nonceAndCipher, NONCE_LEN, ciphertextOnly, 0, ciphertextOnly.length);

        // 5. 构造完整的密文（密文 + Tag），用于 SM4-GCM 解密和完整性校验
        byte[] ciphertextWithTag = new byte[ciphertextOnly.length + tag.length];
        System.arraycopy(ciphertextOnly, 0, ciphertextWithTag, 0, ciphertextOnly.length);
        System.arraycopy(tag, 0, ciphertextWithTag, ciphertextOnly.length, tag.length);

        // 6. SM4-GCM 解密和验证（使用相同的时间戳进行 AAD 验证）
        SecretKeySpec keySpec = new SecretKeySpec(Base62.decode(secretKey), "SM4");
        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LEN, nonce);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        cipher.updateAAD(timestampStr.getBytes(StandardCharsets.UTF_8));
        byte[] plainBytes = cipher.doFinal(ciphertextWithTag);

        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    /**
     * 生成随机 SM4 密钥
     *
     * <p>仅用于生成新密钥或测试用途。生成后应保存到配置中心或 KMS，运行时使用固定密钥。
     * 禁止在每次加密时调用此方法生成新密钥，否则已下发的 Cookie 将无法解密。
     *
     * @return 随机 16 字节（128 位）密钥
     */
    public static byte[] generateKey() {
        return RandomUtil.randomBytes(16);
    }

    /**
     * 演示 main 方法
     *
     * <p>演示如何使用本工具类进行 Cookie 加密和解密。
     * 实际使用时：密钥只需生成一次，保存到配置中重复使用。
     */
    public static void main(String[] args) throws Exception {
        // ========== 演示代码 ==========
        // 1. 生成随机密钥（仅演示用，实际使用时密钥应持久化固定）
        byte[] key = generateKey();
        String secretKey = Base62.encode(key);
        System.out.println("密钥Base62: " + secretKey);
        System.out.println("注意：生产环境请将密钥保存到配置中心，勿每次生成新密钥");

        // 2. 模拟要保护的敏感数据
        String original = "userId=1001&role=admin&expireTime=20251231";

        // 3. 加密 Cookie
        String cookieValue = encrypt(original, secretKey);
        System.out.println("Encrypted Cookie: " + cookieValue);
        System.out.println("Cookie 长度: " + cookieValue.length() + " 字符");

        // 4. 解密验证（设置 1 小时有效期）
        String recovered = decrypt(cookieValue, secretKey, 3600);
        System.out.println("Decrypted value: " + recovered);

        // 5. 验证解密结果与原数据一致
        if (original.equals(recovered)) {
            System.out.println("✓ 加解密验证成功");
        } else {
            System.out.println("✗ 加解密验证失败");
        }
    }
}
