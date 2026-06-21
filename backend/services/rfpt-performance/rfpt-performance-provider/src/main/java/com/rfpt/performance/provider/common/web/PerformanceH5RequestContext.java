package com.rfpt.performance.provider.common.web;

import com.rfpt.performance.provider.common.config.PerformanceH5AuthProperties;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 员工绩效 H5 请求上下文。
 */
@Component
public class PerformanceH5RequestContext {

    /**
     * H5 登录配置。
     */
    @Resource
    private PerformanceH5AuthProperties performanceH5AuthProperties;

    /**
     * 获取登录手机号。
     *
     * @param request HTTP 请求
     * @return 登录手机号
     */
    public String requireMobile(HttpServletRequest request) {
        String mobile = getCookieValue(request, performanceH5AuthProperties.getCookieName());
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(ErrorCode.E999005, "请先登录");
        }
        return mobile;
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
}
