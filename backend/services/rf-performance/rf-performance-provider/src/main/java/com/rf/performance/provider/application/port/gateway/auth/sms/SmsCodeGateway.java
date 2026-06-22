package com.rf.performance.provider.application.port.gateway.auth.sms;

import com.rf.performance.provider.application.port.gateway.auth.sms.param.SmsCodeSendGatewayParam;

/**
 * 短信验证码发送网关。
 */
public interface SmsCodeGateway {

    /**
     * 发送短信验证码。
     *
     * @param param 发送参数
     */
    void sendCode(SmsCodeSendGatewayParam param);
}
