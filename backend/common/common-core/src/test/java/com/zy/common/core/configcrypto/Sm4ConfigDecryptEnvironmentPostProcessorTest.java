package com.zy.common.core.configcrypto;

import com.zy.common.utils.Sm4Util;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SM4 配置密文解密处理器测试。
 */
class Sm4ConfigDecryptEnvironmentPostProcessorTest {

    /**
     * 验证 Spring 读取 SM4 密文配置时返回明文。
     */
    @Test
    void shouldDecryptSm4ConfigValue() {
        String secretKey = "1YY0YMY7oOnkGzBCDn2Uns";
        String cipherText = Sm4Util.encrypt("test123", secretKey);
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "qy.config-crypto.secret-key", secretKey,
                "spring.datasource.password", "SM4_" + cipherText
        )));

        new Sm4ConfigDecryptEnvironmentPostProcessor().postProcessEnvironment(environment, null);

        assertEquals("test123", environment.getProperty("spring.datasource.password"));
    }
}
