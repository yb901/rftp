package com.rf.mng.provider.common.cookie.impl;

import com.alibaba.fastjson2.JSON;
import com.rf.mng.provider.common.cookie.MngCookie;
import com.rf.mng.provider.common.cookie.MngCookieService;
import com.zy.common.utils.Sm4GcmCookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 管理后台 Cookie 服务实现。
 */
@Slf4j
@Service
public class MngCookieServiceImpl implements MngCookieService {

    /** 管理后台 Cookie 加密密钥。 */
    @Value("${rf.mng.cookie.secret-key}")
    private String secretKey;

    /** Cookie 有效期，默认 2 天。 */
    @Value("${rf.mng.cookie.max-age:172800}")
    private long maxAgeSeconds;

    /**
     * 从请求中读取并解密 Cookie。
     *
     * @param request HTTP请求
     * @param cookieName Cookie名称
     * @return Cookie信息
     */
    @Override
    public MngCookie getFromRequest(HttpServletRequest request, String cookieName) {
        String headerValue = request.getHeader(cookieName);
        if (headerValue != null && !headerValue.isBlank()) {
            return decrypt(headerValue);
        }
        Cookie cookie = findServletCookie(request, cookieName);
        if (cookie == null) {
            return null;
        }
        return decrypt(cookie.getValue());
    }

    /**
     * 生成加密 Cookie 值。
     *
     * @param mngCookie Cookie信息
     * @return 加密值
     */
    @Override
    public String generateCookieValue(MngCookie mngCookie) {
        try {
            String plainText = JSON.toJSONString(mngCookie);
            return Sm4GcmCookie.encrypt(plainText, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("管理后台 Cookie 加密失败", e);
        }
    }

    /**
     * 将 Cookie 写入响应。
     *
     * @param response HTTP响应
     * @param mngCookie Cookie信息
     * @param cookieName Cookie名称
     */
    @Override
    public void setToResponse(HttpServletResponse response, MngCookie mngCookie, String cookieName) {
        Cookie cookie = new Cookie(cookieName, generateCookieValue(mngCookie));
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 清理响应 Cookie。
     *
     * @param response HTTP响应
     * @param cookieName Cookie名称
     */
    @Override
    public void clearFromResponse(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 解密 Cookie 值。
     *
     * @param cookieValue Cookie密文
     * @return Cookie信息
     */
    private MngCookie decrypt(String cookieValue) {
        try {
            String plainText = Sm4GcmCookie.decrypt(cookieValue, secretKey, maxAgeSeconds);
            return JSON.parseObject(plainText, MngCookie.class);
        } catch (Exception e) {
            log.warn("管理后台 Cookie 解密失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 查找 Servlet Cookie。
     *
     * @param request HTTP请求
     * @param cookieName Cookie名称
     * @return Cookie对象
     */
    private Cookie findServletCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .orElse(null);
    }
}
