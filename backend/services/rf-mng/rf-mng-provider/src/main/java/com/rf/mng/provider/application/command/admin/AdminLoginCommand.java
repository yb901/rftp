package com.rf.mng.provider.application.command.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员登录命令。
 */
@Data
public class AdminLoginCommand implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名。 */
    private String username;

    /** 登录密码。 */
    private String password;

    /** OTP动态验证码。 */
    private String otpCode;
}
