package com.rf.mng.provider.common.cookie;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理后台登录 Cookie。
 */
@Data
public class MngCookie implements Serializable {

    /** 管理后台 Cookie 名称。 */
    public static final String MNG_COOKIE_NAME = "mngAuthToken";

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 管理员ID。 */
    private Long adminId;

    /** 用户名。 */
    private String username;

    /** 真实姓名。 */
    private String realName;

    /** 角色编码。 */
    private String role;
}
