package com.rf.mng.provider.common.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 管理后台 Cookie 服务。
 */
public interface MngCookieService {

    /**
     * 从请求中读取并解密 Cookie。
     *
     * @param request HTTP请求
     * @param cookieName Cookie名称
     * @return Cookie信息
     */
    MngCookie getFromRequest(HttpServletRequest request, String cookieName);

    /**
     * 生成加密 Cookie 值。
     *
     * @param mngCookie Cookie信息
     * @return 加密值
     */
    String generateCookieValue(MngCookie mngCookie);

    /**
     * 将 Cookie 写入响应。
     *
     * @param response HTTP响应
     * @param mngCookie Cookie信息
     * @param cookieName Cookie名称
     */
    void setToResponse(HttpServletResponse response, MngCookie mngCookie, String cookieName);

    /**
     * 清理响应 Cookie。
     *
     * @param response HTTP响应
     * @param cookieName Cookie名称
     */
    void clearFromResponse(HttpServletResponse response, String cookieName);
}
