package com.zy.common.core.order;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Set;

/**
 * 单号环境解析器
 *
 * <p>根据当前应用的 Spring Profile 解析订单号环境位，生产环境使用生产编码，其他环境默认使用测试编码。</p>
 *
 * @author zzy
 * @date 2026/06/09
 */
public final class OrderEnvironmentResolver {

    /**
     * 生产环境 Profile 集合
     */
    private static final Set<String> PROD_PROFILES = Set.of("prod", "production");

    /**
     * 私有构造方法
     */
    private OrderEnvironmentResolver() {
    }

    /**
     * 解析当前应用单号环境
     *
     * @param environment Spring 环境对象
     * @return 单号环境
     */
    public static OrderEnvironmentEnum resolve(Environment environment) {
        if (environment == null || environment.getActiveProfiles() == null) {
            return OrderEnvironmentEnum.TEST;
        }
        boolean prod = Arrays.stream(environment.getActiveProfiles())
                .filter(profile -> profile != null && !profile.isBlank())
                .map(profile -> profile.trim().toLowerCase())
                .anyMatch(PROD_PROFILES::contains);
        return prod ? OrderEnvironmentEnum.PROD : OrderEnvironmentEnum.TEST;
    }
}
