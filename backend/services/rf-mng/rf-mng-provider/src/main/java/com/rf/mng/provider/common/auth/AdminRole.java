package com.rf.mng.provider.common.auth;

import java.util.Arrays;
import java.util.Set;

/**
 * 管理后台角色枚举。
 */
public enum AdminRole {

    /** 超级管理员。 */
    SUPER_ADMIN(1, "超级管理员", Set.of(MngModule.SOCIAL_SECURITY, MngModule.PERFORMANCE, MngModule.ADMIN)),

    /** 管理员。 */
    ADMIN(2, "管理员", Set.of(MngModule.SOCIAL_SECURITY, MngModule.PERFORMANCE)),

    /** 社保专员。 */
    SOCIAL_SECURITY_SPECIALIST(3, "社保专员", Set.of(MngModule.SOCIAL_SECURITY)),

    /** 绩效专员。 */
    PERFORMANCE_SPECIALIST(4, "绩效专员", Set.of(MngModule.PERFORMANCE));

    /** 角色编码。 */
    private final int code;

    /** 角色名称。 */
    private final String label;

    /** 可访问模块。 */
    private final Set<MngModule> modules;

    /**
     * 构造管理后台角色。
     *
     * @param code 角色编码
     * @param label 角色名称
     * @param modules 可访问模块
     */
    AdminRole(int code, String label, Set<MngModule> modules) {
        this.code = code;
        this.label = label;
        this.modules = modules;
    }

    /**
     * 判断角色是否有模块权限。
     *
     * @param module 管理后台模块
     * @return 是否有权限
     */
    public boolean hasModule(MngModule module) {
        return module != null && modules.contains(module);
    }

    /**
     * 根据编码查找角色。
     *
     * @param code 角色编码
     * @return 角色枚举
     */
    public static AdminRole of(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(role -> role.code == code)
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断角色编码是否有效。
     *
     * @param code 角色编码
     * @return 是否有效
     */
    public static boolean valid(Integer code) {
        return of(code) != null;
    }

    /**
     * 获取角色编码。
     *
     * @return 角色编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取角色名称。
     *
     * @return 角色名称
     */
    public String getLabel() {
        return label;
    }
}
