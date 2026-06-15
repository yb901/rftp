package com.zy.common.core.configcrypto;

import com.zy.common.utils.Sm4Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * SM4 配置密文解密处理器。
 *
 * <p>配置值使用 {@code SM4_密文} 格式时，本处理器在 Spring 读取配置值时自动解密。</p>
 */
public class Sm4ConfigDecryptEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * 配置密文前缀。
     */
    private static final String CIPHER_PREFIX = "SM4_";

    /**
     * 解密密钥配置项。
     */
    private static final String SECRET_KEY_PROPERTY = "qy.config-crypto.secret-key";

    /**
     * 解密密钥环境变量。
     */
    private static final String SECRET_KEY_ENV = "QY_CONFIG_CRYPTO_SECRET_KEY";

    /**
     * 是否启用配置解密。
     */
    private static final String ENABLED_PROPERTY = "qy.config-crypto.enabled";

    /**
     * 在配置数据加载后执行，确保 Nacos 配置已经进入环境。
     *
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 包装所有可枚举配置源，读取 {@code SM4_...} 值时自动解密。
     *
     * @param environment Spring 环境
     * @param application Spring 应用
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isEnabled(environment)) {
            return;
        }
        String secretKey = resolveSecretKey(environment);
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof Sm4DecryptPropertySource) {
                continue;
            }
            if (propertySource instanceof EnumerablePropertySource<?> enumerablePropertySource) {
                propertySources.replace(propertySource.getName(), new Sm4DecryptPropertySource(enumerablePropertySource, secretKey));
            }
        }
    }

    /**
     * 判断配置解密是否启用。
     *
     * @param environment Spring 环境
     * @return 是否启用
     */
    private boolean isEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(ENABLED_PROPERTY, Boolean.class, true);
    }

    /**
     * 解析配置解密密钥。
     *
     * @param environment Spring 环境
     * @return Base62 编码的 SM4 密钥
     */
    private String resolveSecretKey(ConfigurableEnvironment environment) {
        String secretKey = environment.getProperty(SECRET_KEY_PROPERTY);
        if (StringUtils.hasText(secretKey)) {
            return secretKey;
        }
        return environment.getProperty(SECRET_KEY_ENV);
    }

    /**
     * 支持 SM4 密文自动解密的配置源包装器。
     */
    private static class Sm4DecryptPropertySource extends EnumerablePropertySource<EnumerablePropertySource<?>> {

        /**
         * Base62 编码的 SM4 密钥。
         */
        private final String secretKey;

        /**
         * 创建配置源包装器。
         *
         * @param delegate 原始配置源
         * @param secretKey Base62 编码的 SM4 密钥
         */
        Sm4DecryptPropertySource(EnumerablePropertySource<?> delegate, String secretKey) {
            super(delegate.getName(), delegate);
            this.secretKey = secretKey;
        }

        /**
         * 获取配置项名称列表。
         *
         * @return 配置项名称列表
         */
        @Override
        public String[] getPropertyNames() {
            return source.getPropertyNames();
        }

        /**
         * 获取配置值，遇到 SM4 密文格式时自动解密。
         *
         * @param name 配置项名称
         * @return 配置值
         */
        @Override
        public Object getProperty(String name) {
            Object value = source.getProperty(name);
            if (!(value instanceof String text) || !isCipherText(text)) {
                return value;
            }
            if (!StringUtils.hasText(secretKey)) {
                throw new IllegalStateException("配置项 " + name + " 使用 SM4 密文，但未配置 " + SECRET_KEY_ENV);
            }
            return Sm4Util.decrypt(extractCipherText(text), secretKey);
        }

        /**
         * 判断是否为 SM4 配置密文格式。
         *
         * @param value 配置值
         * @return 是否为密文
         */
        private boolean isCipherText(String value) {
            return value.startsWith(CIPHER_PREFIX);
        }

        /**
         * 提取 SM4 密文正文。
         *
         * @param value 配置值
         * @return 密文正文
         */
        private String extractCipherText(String value) {
            return value.substring(CIPHER_PREFIX.length());
        }
    }
}
