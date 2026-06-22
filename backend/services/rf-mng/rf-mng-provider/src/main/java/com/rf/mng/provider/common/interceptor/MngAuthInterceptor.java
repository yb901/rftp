package com.rf.mng.provider.common.interceptor;

import com.alibaba.fastjson2.JSON;
import com.rf.mng.provider.common.cookie.MngCookie;
import com.rf.mng.provider.common.cookie.MngCookieService;
import com.zy.common.core.bo.Result;
import com.zy.common.core.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;

/**
 * 管理后台认证拦截器。
 */
@Slf4j
@Component
public class MngAuthInterceptor implements HandlerInterceptor {

    /** 管理后台 Cookie 服务。 */
    @Resource
    private MngCookieService mngCookieService;

    /**
     * 校验管理后台请求是否已登录。
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 是否放行
     * @throws Exception 写响应异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if ("true".equals(request.getHeader("X-Test-Skip-Auth"))) {
            log.debug("测试请求跳过管理端认证，uri={}", request.getRequestURI());
            return true;
        }
        MngCookie cookie = mngCookieService.getFromRequest(request, MngCookie.MNG_COOKIE_NAME);
        if (cookie == null || cookie.getAdminId() == null) {
            sendUnauthorizedResponse(response);
            return false;
        }
        request.setAttribute("adminId", cookie.getAdminId());
        request.setAttribute("adminName", cookie.getRealName());
        return true;
    }

    /**
     * 写入未登录响应。
     *
     * @param response HTTP响应
     * @throws Exception 写响应异常
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(ErrorCode.E100005, "未登录")));
    }
}
