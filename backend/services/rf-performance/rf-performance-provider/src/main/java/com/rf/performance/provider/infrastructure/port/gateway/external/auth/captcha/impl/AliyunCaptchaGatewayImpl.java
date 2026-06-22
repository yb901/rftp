package com.rf.performance.provider.infrastructure.port.gateway.external.auth.captcha.impl;

import com.aliyun.captcha20230305.Client;
import com.aliyun.captcha20230305.models.VerifyCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyCaptchaResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.rf.performance.provider.application.port.gateway.auth.captcha.CaptchaGateway;
import com.rf.performance.provider.application.port.gateway.auth.captcha.param.CaptchaVerifyGatewayParam;
import com.rf.performance.provider.common.config.PerformanceSmsProperties;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 阿里云图形验证码校验网关实现。
 * 外部接口文档：https://next.api.aliyun.com/api-tools/demo/captcha/0decc0c7-1990-4b09-b55e-a5a79ff2cc70
 */
@Slf4j
@Component
public class AliyunCaptchaGatewayImpl implements CaptchaGateway {

    /**
     * 短信与验证码配置。
     */
    @Resource
    private PerformanceSmsProperties performanceSmsProperties;

    /**
     * 校验图形验证码。
     *
     * @param param 校验参数
     */
    @Override
    public void verify(CaptchaVerifyGatewayParam param) {
        CaptchaVerifyGatewayParam safeParam = param == null ? new CaptchaVerifyGatewayParam() : param;
        validateParam(safeParam);
        validateConfig();
        try {
            VerifyCaptchaResponse response = createClient().verifyCaptcha(buildRequest(safeParam));
            assertSuccess(response);
        } catch (BusinessException e) {
            throw e;
        } catch (TeaException e) {
            log.error("阿里云验证码校验异常, code={}, statusCode={}, message={}, data={}",
                    e.getCode(), e.getStatusCode(), e.getMessage(), e.getData(), e);
            throw new BusinessException(ErrorCode.E999002, "验证码校验失败");
        } catch (Exception e) {
            log.error("阿里云验证码校验异常", e);
            throw new BusinessException(ErrorCode.E999002, "验证码校验失败");
        }
    }

    /**
     * 构建阿里云验证码客户端。
     *
     * @return 阿里云验证码客户端
     * @throws Exception 创建异常
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(performanceSmsProperties.getAccessKeyId())
                .setAccessKeySecret(performanceSmsProperties.getAccessKeySecret())
                .setRegionId(performanceSmsProperties.getCaptchaRegionId())
                .setEndpoint(performanceSmsProperties.getCaptchaEndpoint());
        return new Client(config);
    }

    /**
     * 构建验证码校验请求。
     *
     * @param param 校验参数
     * @return 验证码校验请求
     */
    private VerifyCaptchaRequest buildRequest(CaptchaVerifyGatewayParam param) {
        return new VerifyCaptchaRequest().setCaptchaVerifyParam(param.getCaptchaVerifyParam());
    }

    /**
     * 校验验证码响应。
     *
     * @param response 验证码响应
     */
    private void assertSuccess(VerifyCaptchaResponse response) {
        Boolean verifyResult = response == null
                || response.getBody() == null
                || response.getBody().getResult() == null
                ? null
                : response.getBody().getResult().getVerifyResult();
        if (Boolean.TRUE.equals(verifyResult)) {
            return;
        }
        log.warn("阿里云验证码校验失败, response={}", response == null ? null : response.getBody());
        throw new BusinessException(ErrorCode.E999001, "请先完成验证码校验");
    }

    /**
     * 校验请求参数。
     *
     * @param param 校验参数
     */
    private void validateParam(CaptchaVerifyGatewayParam param) {
        if (StringUtils.isBlank(param.getCaptchaVerifyParam())) {
            throw new BusinessException(ErrorCode.E999001, "请先完成验证码校验");
        }
    }

    /**
     * 校验阿里云验证码配置。
     */
    private void validateConfig() {
        if (StringUtils.isAnyBlank(performanceSmsProperties.getAccessKeyId(),
                performanceSmsProperties.getAccessKeySecret(),
                performanceSmsProperties.getCaptchaRegionId(),
                performanceSmsProperties.getCaptchaEndpoint())) {
            throw new BusinessException(ErrorCode.E999002, "阿里云验证码配置不完整");
        }
    }
}
