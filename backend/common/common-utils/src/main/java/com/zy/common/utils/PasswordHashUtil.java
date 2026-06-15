package com.zy.common.utils;

import cn.hutool.core.codec.Base62;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

/**
 * PBKDF2 密码哈希工具类（使用国密 SM3 作为 HMAC 算法）
 * <p>
 * 本工具类基于 PBKDF2（Password-Based Key Derivation Function 2）标准，结合 HMAC-SM3 算法，
 * 生成安全的密码哈希值，用于用户密码的存储与验证。
 * </p>
 * <p>
 * 主要特性：
 * <ul>
 *     <li>自动生成随机盐值（16 字节），防止彩虹表攻击</li>
 *     <li>固定迭代次数（10000 次），增加暴力破解成本</li>
 *     <li>输出 256 位（32 字节）的派生密钥，安全性高</li>
 *     <li>使用 Base62 编码存储盐值和哈希值，格式紧凑且无歧义</li>
 *     <li>存储格式为 "Base62(盐值):Base62(哈希值)"，便于单字段保存</li>
 * </ul>
 * </p>
 *
 * @author zzy
 * @date 2026/05/05 09:05
 */
public class PasswordHashUtil {

    /**
     * PBKDF2 迭代次数
     * <p>
     * 迭代次数直接影响计算耗时，越高则暴力破解成本越大。
     * 推荐值至少 10000，可根据服务器性能适当调高（如 20000 或更高）。
     * </p>
     */
    private static final int ITERATIONS = 10000;

    /**
     * 生成密钥长度（单位：位）
     * <p>
     * 256 位（32 字节）是当前广泛认可的安全长度，可抵抗暴力攻击。
     * </p>
     */
    private static final int KEY_LENGTH = 256;

    /**
     * 盐值长度（单位：字节）
     * <p>
     * 16 字节（128 位）的随机盐值足以防止彩虹表预计算攻击。
     * </p>
     */
    private static final int SALT_LENGTH = 16;

    /**
     * PBKDF2 使用的算法名称
     * <p>
     * 格式：PBKDF2WithHmacSM3，表示使用 HMAC-SM3 作为伪随机函数。
     * 需要 Bouncy Castle 提供者支持。
     * </p>
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSM3";

    // ======================== 静态初始化块 ========================

    static {
        // 注册 Bouncy Castle 安全提供者，使 JCE 能够识别 SM3 等国产算法
        Security.addProvider(new BouncyCastleProvider());
    }

    // ======================== 公共 API ========================

    /**
     * 生成密码哈希值（自动生成随机盐）
     * <p>
     * 该方法会生成一个随机的 16 字节盐值，然后使用 PBKDF2WithHmacSM3 算法计算密码的哈希值。
     * 最终返回格式为 "Base62(盐值):Base62(哈希值)" 的字符串。
     * </p>
     *
     * @param password 明文密码（不允许为 null）
     * @return 格式如 "5Y9j3Io5yB9rrK4k9HdCEg:8t0it6S734MxZwvSY75sXVZ5qbWt5XhD4YpSVcKbzCX" 的哈希字符串
     * @throws RuntimeException 如果算法不可用或密钥生成失败（包装原始异常）
     */
    public static String hashPassword(String password) {
        try {
            // 1. 生成随机盐值
            byte[] salt = generateSalt();
            // 2. 计算 PBKDF2 哈希（派生密钥）
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            // 3. 使用 Base62 编码盐值和哈希值（编码结果只包含 0-9A-Za-z，不含冒号等特殊字符）
            String saltB62 = Base62.encode(salt);
            String hashB62 = Base62.encode(hash);
            // 4. 用冒号分隔并返回
            return saltB62 + ":" + hashB62;
        } catch (Exception e) {
            // 将受检异常包装为运行时异常，简化调用方处理（生产环境可根据需求定制）
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * 验证密码是否与存储的哈希值匹配
     * <p>
     * 从存储的哈希字符串中解析出盐值和期望的哈希值，使用相同的 PBKDF2 参数重新计算密码的哈希，
     * 然后通过恒定时间比较（MessageDigest.isEqual）防止时序攻击。
     * </p>
     *
     * @param password   待验证的明文密码
     * @param storedHash 存储的哈希字符串，格式必须为 "Base62(盐值):Base62(哈希值)"
     * @return 如果密码匹配返回 true，否则返回 false
     * @throws IllegalArgumentException 如果 storedHash 格式不正确（不包含冒号或分割后不是两部分）
     * @throws RuntimeException         如果算法不可用或解码失败（包装原始异常）
     */
    public static boolean verifyPassword(String password, String storedHash) {
        // 1. 解析存储的哈希：按冒号分割为盐值部分和哈希值部分
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid stored hash format, expected 'salt:hash'");
        }

        try {
            // 2. Base62 解码得到原始盐值和期望的哈希值
            byte[] salt = Base62.decode(parts[0]);
            byte[] expectedHash = Base62.decode(parts[1]);

            // 3. 使用相同的参数重新计算密码的哈希
            byte[] computedHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // 4. 恒定时间比较，避免时序泄漏
            return MessageDigest.isEqual(computedHash, expectedHash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify password", e);
        }
    }

    // ======================== 私有辅助方法 ========================

    /**
     * 生成随机盐值
     * <p>
     * 使用 {@link SecureRandom} 生成密码学安全的随机字节序列。
     * </p>
     *
     * @return 长度为 {@link #SALT_LENGTH} 的随机字节数组
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * PBKDF2 密钥派生核心逻辑
     * <p>
     * 根据密码、盐值、迭代次数和期望密钥长度，计算派生密钥。
     * 内部使用 {@link SecretKeyFactory} 和 {@link PBEKeySpec} 实现。
     * </p>
     *
     * @param password   字符数组形式的密码（使用后应尽快清空，此处由调用方负责）
     * @param salt       盐值字节数组
     * @param iterations 迭代次数
     * @param keyLength  输出密钥长度（位）
     * @return 派生密钥的字节数组（长度为 keyLength/8）
     * @throws NoSuchAlgorithmException 如果算法名称不支持（通常不会发生，因为已注册 Bouncy Castle）
     * @throws InvalidKeySpecException  如果密钥规格无效（例如密码或盐值参数异常）
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // PBEKeySpec 封装了密码、盐值、迭代次数和输出密钥长度
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        // 获取支持 PBKDF2WithHmacSM3 算法的密钥工厂
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        // 生成密钥（SecretKey 对象）
        SecretKey key = factory.generateSecret(spec);
        // 返回原始密钥字节
        return key.getEncoded();
    }

    // ======================== 测试入口 ========================

    /**
     * 简单测试 main 方法，演示哈希生成与验证流程
     * <p>
     * 运行本方法将输出：
     * <ul>
     *     <li>原始密码和生成的 Base62 哈希串</li>
     *     <li>正确密码验证结果（应通过）</li>
     *     <li>错误密码验证结果（应失败）</li>
     *     <li>同一密码两次哈希结果不同（因为盐值随机）</li>
     * </ul>
     * </p>
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        try {
            String originalPassword = "MySecureP@ssw0rd!";

            // 生成密码哈希（含随机盐）
            String storedHash = hashPassword(originalPassword);
            System.out.println("原始密码: " + originalPassword);
            System.out.println("存储的哈希值 (Base62编码): " + storedHash);
            System.out.println("哈希长度: " + storedHash.length() + " 字符\n");

            // 验证正确的密码
            boolean match = verifyPassword(originalPassword, storedHash);
            System.out.println("验证正确密码 '" + originalPassword + "' → " + (match ? "通过" : "失败"));

            // 验证错误的密码
            String wrongPassword = "WrongPassword123";
            boolean wrongMatch = verifyPassword(wrongPassword, storedHash);
            System.out.println("验证错误密码 '" + wrongPassword + "' → " + (wrongMatch ? "通过" : "失败"));

            // 再次对同一原始密码生成哈希（由于盐值随机，结果必然不同）
            String anotherHash = hashPassword(originalPassword);
            System.out.println("\n同一密码再次哈希: " + anotherHash);
            System.out.println("两次哈希是否相同: " + storedHash.equals(anotherHash));

        } catch (Exception e) {
            System.err.println("加密或验证过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}