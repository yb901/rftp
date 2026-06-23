package com.rf.performance.provider.common.web;

import com.rf.performance.provider.common.config.PerformanceWebAuthProperties;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * 员工绩效 H5 请求上下文。
 */
@Component
public class PerformanceH5RequestContext {

    /**
     * 员工端 Web 登录配置。
     */
    @Resource
    private PerformanceWebAuthProperties performanceWebAuthProperties;

    /**
     * HMAC SHA256 算法。
     */
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 获取登录手机号。
     *
     * @param request HTTP 请求
     * @return 登录手机号
     */
    public String requireMobile(HttpServletRequest request) {
        String token = getCookieValue(request, PerformanceWebAuthProperties.COOKIE_NAME);
        String mobile = parseLoginToken(token);
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(ErrorCode.E999005, "请先登录");
        }
        return mobile;
    }

    /**
     * 创建登录令牌。
     *
     * @param mobile 登录手机号
     * @return 登录令牌
     */
    public String createLoginToken(String mobile) {
        long timestamp = Instant.now().getEpochSecond();
        String payload = mobile + "." + timestamp;
        return payload + "." + sign(payload);
    }

    /**
     * 获取客户端 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取浏览器 User-Agent。
     *
     * @param request HTTP 请求
     * @return 浏览器 User-Agent
     */
    public String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * 获取 Cookie 值。
     *
     * @param request HTTP 请求
     * @param name Cookie 名称
     * @return Cookie 值
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (StringUtils.equals(cookie.getName(), name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 解析登录令牌。
     *
     * @param token 登录令牌
     * @return 手机号
     */
    private String parseLoginToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String payload = parts[0] + "." + parts[1];
        if (!StringUtils.equals(parts[2], sign(payload))) {
            return null;
        }
        long timestamp = parseTimestamp(parts[1]);
        long ageSeconds = Instant.now().getEpochSecond() - timestamp;
        if (timestamp <= 0 || ageSeconds < 0 || ageSeconds > performanceWebAuthProperties.getCookieMaxAgeSeconds()) {
            return null;
        }
        return parts[0];
    }

    /**
     * 解析时间戳。
     *
     * @param value 时间戳文本
     * @return 时间戳
     */
    private long parseTimestamp(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    /**
     * 对文本签名。
     *
     * @param value 待签名文本
     * @return 签名
     */
    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            String secret = StringUtils.defaultIfBlank(performanceWebAuthProperties.getCookieSecret(), "rf-performance-change-me");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.E999002, "登录令牌签名失败");
        }
    }
}
