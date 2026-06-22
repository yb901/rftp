package com.rf.performance.provider.infrastructure.port.gateway.external.auth.sms.impl;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.rf.performance.provider.application.port.gateway.auth.sms.SmsCodeGateway;
import com.rf.performance.provider.application.port.gateway.auth.sms.param.SmsCodeSendGatewayParam;
import com.rf.performance.provider.common.config.PerformanceSmsProperties;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 阿里云短信验证码发送网关实现。
 * 外部接口文档：https://api.aliyun.com/api-tools/sdk/Dysmsapi?version=2017-05-25
 */
@Slf4j
@Component
public class AliyunSmsCodeGatewayImpl implements SmsCodeGateway {

    /**
     * 阿里云短信成功编码。
     */
    private static final String OK_CODE = "OK";

    /**
     * 短信与验证码配置。
     */
    @Resource
    private PerformanceSmsProperties performanceSmsProperties;

    /**
     * 发送短信验证码。
     *
     * @param param 发送参数
     */
    @Override
    public void sendCode(SmsCodeSendGatewayParam param) {
        SmsCodeSendGatewayParam safeParam = param == null ? new SmsCodeSendGatewayParam() : param;
        validateParam(safeParam);
        validateConfig();
        try {
            SendSmsResponse response = createClient().sendSms(buildRequest(safeParam));
            assertSuccess(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("阿里云短信发送异常, mobile={}", maskMobile(safeParam.getMobile()), e);
            throw new BusinessException(ErrorCode.E999002, "短信发送失败");
        }
    }

    /**
     * 构建阿里云短信客户端。
     *
     * @return 阿里云短信客户端
     * @throws Exception 创建异常
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(performanceSmsProperties.getAccessKeyId())
                .setAccessKeySecret(performanceSmsProperties.getAccessKeySecret())
                .setRegionId(performanceSmsProperties.getRegionId())
                .setEndpoint(performanceSmsProperties.getEndpoint());
        return new Client(config);
    }

    /**
     * 构建发送短信请求。
     *
     * @param param 发送参数
     * @return 发送短信请求
     */
    private SendSmsRequest buildRequest(SmsCodeSendGatewayParam param) {
        String templateParam = JSON.toJSONString(Map.of(
                performanceSmsProperties.getCodeTemplateParamName(), param.getCode()
        ));
        return new SendSmsRequest()
                .setPhoneNumbers(param.getMobile())
                .setSignName(performanceSmsProperties.getSignName())
                .setTemplateCode(performanceSmsProperties.getTemplateCode())
                .setTemplateParam(templateParam);
    }

    /**
     * 校验发送结果。
     *
     * @param response 阿里云短信响应
     */
    private void assertSuccess(SendSmsResponse response) {
        String code = response == null || response.getBody() == null ? null : response.getBody().getCode();
        if (OK_CODE.equals(code)) {
            return;
        }
        String message = response == null || response.getBody() == null ? null : response.getBody().getMessage();
        log.warn("阿里云短信发送失败, code={}, message={}", code, message);
        throw new BusinessException(ErrorCode.E999002, "短信发送失败");
    }

    /**
     * 校验发送参数。
     *
     * @param param 发送参数
     */
    private void validateParam(SmsCodeSendGatewayParam param) {
        if (StringUtils.isBlank(param.getMobile())) {
            throw new BusinessException(ErrorCode.E999001, "手机号不能为空");
        }
        if (StringUtils.isBlank(param.getCode())) {
            throw new BusinessException(ErrorCode.E999001, "验证码不能为空");
        }
    }

    /**
     * 校验阿里云短信配置。
     */
    private void validateConfig() {
        if (StringUtils.isAnyBlank(performanceSmsProperties.getAccessKeyId(),
                performanceSmsProperties.getAccessKeySecret(),
                performanceSmsProperties.getSignName(),
                performanceSmsProperties.getTemplateCode(),
                performanceSmsProperties.getEndpoint(),
                performanceSmsProperties.getCodeTemplateParamName())) {
            throw new BusinessException(ErrorCode.E999002, "阿里云短信配置不完整");
        }
    }

    /**
     * 手机号脱敏。
     *
     * @param mobile 手机号
     * @return 脱敏手机号
     */
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
