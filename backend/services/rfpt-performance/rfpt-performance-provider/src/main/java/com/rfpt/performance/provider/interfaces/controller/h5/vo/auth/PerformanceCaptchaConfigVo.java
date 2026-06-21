package com.rfpt.performance.provider.interfaces.controller.h5.vo.auth;

import lombok.Data;

/**
 * 员工绩效 H5 图形验证码配置 VO。
 */
@Data
public class PerformanceCaptchaConfigVo {

    /**
     * 是否启用图形验证码。
     */
    private Boolean enabled;

    /**
     * 阿里云验证码前端地域。
     */
    private String region;

    /**
     * 阿里云验证码前端身份标识。
     */
    private String prefix;

    /**
     * 阿里云验证码场景 ID。
     */
    private String sceneId;

    /**
     * 阿里云验证码语言。
     */
    private String language;

    /**
     * 阿里云验证码 JS 地址。
     */
    private String jsUrl;
}
