package com.rf.mng.provider.application.port.persistence.admin.record;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员读取记录。
 */
@Data
public class AdminRecord implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 管理员ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 真实姓名。 */
    private String realName;

    /** 登录密码密文。 */
    private String password;

    /** OTP二次验证密钥。 */
    private String otpSecret;

    /** 是否启用：0-否，1-是。 */
    private Integer enabled;

    /** 角色编码。 */
    private Integer role;

    /** 创建时间。 */
    private LocalDateTime gmtCreate;

    /** 修改时间。 */
    private LocalDateTime gmtModified;
}
