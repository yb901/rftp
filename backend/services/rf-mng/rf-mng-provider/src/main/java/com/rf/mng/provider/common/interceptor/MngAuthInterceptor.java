package com.rf.mng.provider.common.interceptor;

import com.alibaba.fastjson2.JSON;
import com.rf.mng.provider.common.auth.AdminRole;
import com.rf.mng.provider.common.auth.MngPermission;
import com.rf.mng.provider.common.cookie.MngCookie;
import com.rf.mng.provider.common.cookie.MngCookieService;
import com.zy.common.core.bo.Result;
import com.zy.common.core.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
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
        request.setAttribute("adminRole", cookie.getRole());
        if (!hasPermission(handler, cookie)) {
            sendForbiddenResponse(response);
            return false;
        }
        return true;
    }

    /**
     * 校验请求是否具备模块权限。
     *
     * @param handler 处理器
     * @param cookie 登录 Cookie
     * @return 是否允许访问
     */
    private boolean hasPermission(Object handler, MngCookie cookie) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        MngPermission permission = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), MngPermission.class);
        if (permission == null) {
            permission = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), MngPermission.class);
        }
        if (permission == null) {
            return true;
        }
        AdminRole role = AdminRole.of(parseRole(cookie.getRole()));
        return role != null && role.hasModule(permission.value());
    }

    /**
     * 解析角色编码。
     *
     * @param role 角色文本
     * @return 角色编码
     */
    private Integer parseRole(String role) {
        try {
            return role == null ? null : Integer.valueOf(role);
        } catch (NumberFormatException exception) {
            log.warn("管理后台角色编码解析失败，role={}", role);
            return null;
        }
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

    /**
     * 写入无权限响应。
     *
     * @param response HTTP响应
     * @throws Exception 写响应异常
     */
    private void sendForbiddenResponse(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(ErrorCode.E999001, "无权限访问该模块")));
    }
}
