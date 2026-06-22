package com.rf.performance.provider.application.port.gateway.auth.sms.param;

import lombok.Data;

/**
 * 短信验证码发送网关参数。
 */
@Data
public class SmsCodeSendGatewayParam {

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 短信验证码。
     */
    private String code;
}
