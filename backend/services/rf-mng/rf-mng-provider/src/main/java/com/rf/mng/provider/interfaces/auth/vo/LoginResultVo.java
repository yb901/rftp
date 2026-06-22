package com.rf.mng.provider.interfaces.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理后台登录结果视图。
 */
@Data
public class LoginResultVo implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录令牌，当前使用 HttpOnly Cookie 承载。 */
    private String token;

    /** 登录用户信息。 */
    private LoginUserVo user;
}
