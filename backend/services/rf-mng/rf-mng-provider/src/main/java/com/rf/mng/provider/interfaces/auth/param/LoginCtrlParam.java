package com.rf.mng.provider.interfaces.auth.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理后台登录参数。
 */
@Data
public class LoginCtrlParam implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名。 */
    private String username;

    /** 登录密码。 */
    private String password;
}
