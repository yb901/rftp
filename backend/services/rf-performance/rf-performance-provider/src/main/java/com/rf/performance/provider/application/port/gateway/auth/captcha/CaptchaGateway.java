package com.rf.performance.provider.application.port.gateway.auth.captcha;

import com.rf.performance.provider.application.port.gateway.auth.captcha.param.CaptchaVerifyGatewayParam;

/**
 * 图形验证码校验网关。
 */
public interface CaptchaGateway {

    /**
     * 校验图形验证码。
     *
     * @param param 校验参数
     */
    void verify(CaptchaVerifyGatewayParam param);
}
