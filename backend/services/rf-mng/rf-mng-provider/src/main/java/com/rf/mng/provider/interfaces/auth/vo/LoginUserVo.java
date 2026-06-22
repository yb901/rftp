package com.rf.mng.provider.interfaces.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理后台登录用户视图。
 */
@Data
public class LoginUserVo implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 管理员ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 真实姓名。 */
    private String realName;

    /** 角色编码。 */
    private Integer role;
}
