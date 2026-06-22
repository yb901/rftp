package com.rf.performance.provider.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 员工绩效短信与验证码配置。
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "rf-performance.sms")
public class PerformanceSmsProperties {

    /**
     * 是否启用 mock 短信发送。
     */
    private Boolean mockEnabled = Boolean.TRUE;

    /**
     * mock 短信验证码。
     */
    private String mockCode = "123456";

    /**
     * 阿里云短信地域。
     */
    private String regionId = "cn-hangzhou";

    /**
     * 阿里云短信 endpoint。
     */
    private String endpoint = "dysmsapi.aliyuncs.com";

    /**
     * 阿里云访问密钥 ID。
     */
    private String accessKeyId;

    /**
     * 阿里云访问密钥 Secret。
     */
    private String accessKeySecret;

    /**
     * 阿里云短信签名名称。
     */
    private String signName;

    /**
     * 阿里云短信模板编码。
     */
    private String templateCode;

    /**
     * 短信模板中验证码变量名。
     */
    private String codeTemplateParamName = "code";

    /**
     * 是否启用发送前图形验证码校验。
     */
    private Boolean captchaEnabled = Boolean.TRUE;

    /**
     * 阿里云验证码地域。
     */
    private String captchaRegionId = "cn-shanghai";

    /**
     * 阿里云验证码 endpoint。
     */
    private String captchaEndpoint = "captcha.cn-shanghai.aliyuncs.com";

    /**
     * 阿里云验证码前端地域。
     */
    private String captchaRegion = "cn";

    /**
     * 阿里云验证码前端身份标识。
     */
    private String captchaPrefix;

    /**
     * 阿里云验证码场景 ID。
     */
    private String captchaSceneId;

    /**
     * 阿里云验证码语言。
     */
    private String captchaLanguage = "cn";

    /**
     * 阿里云验证码 js 地址。
     */
    private String captchaJsUrl = "https://o.alicdn.com/captcha-frontend/aliyunCaptcha/AliyunCaptcha.js";
}
