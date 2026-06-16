package com.zy.rfpt.mng.provider.infrastructure.port.gateway.internal.robot.tax.impl;

import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.rfpt.mng.provider.application.port.gateway.robot.tax.TaxRobotGateway;
import com.zy.rfpt.mng.provider.infrastructure.port.gateway.internal.robot.tax.config.TaxRobotProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;

/**
 * 税务机器人 HTTP 网关实现。
 */
@Slf4j
@Component
public class TaxRobotGatewayImpl implements TaxRobotGateway {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** 税务机器人配置。 */
    @Resource
    private TaxRobotProperties properties;

    @Override
    public void triggerSocialSecurityPayment(String taxNo, String siteType, String settleMonth) {
        if (StringUtils.isBlank(properties.getBaseUrl())) {
            log.warn("税务机器人地址为空，跳过触发，taxNo={}", taxNo);
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofMillis(properties.getTimeoutMillis()))
                .build();
        String body = "{\"taxNo\":\"" + taxNo + "\",\"siteType\":\"" + StringUtils.defaultIfBlank(siteType, "default") + "\",\"settleMonth\":\"" + StringUtils.defaultString(settleMonth) + "\"}";
        Request.Builder builder = new Request.Builder()
                .url(StringUtils.removeEnd(properties.getBaseUrl(), "/") + "/internal/tax/social-security-payment")
                .post(RequestBody.create(body, JSON));
        if (StringUtils.isNotBlank(properties.getInternalToken())) {
            builder.header("x-internal-token", properties.getInternalToken());
        }
        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new BusinessException(ErrorCode.E999002, "触发税务机器人失败，HTTP状态：" + response.code());
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.E999002, "触发税务机器人异常：" + e.getMessage());
        }
    }
}
