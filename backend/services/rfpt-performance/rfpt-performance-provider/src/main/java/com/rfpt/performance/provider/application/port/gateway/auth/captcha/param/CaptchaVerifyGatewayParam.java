package com.rfpt.performance.provider.application.port.gateway.auth.captcha.param;

import lombok.Data;

/**
 * 图形验证码校验网关参数。
 */
@Data
public class CaptchaVerifyGatewayParam {

    /**
     * 阿里云验证码校验参数。
     */
    private String captchaVerifyParam;
}
