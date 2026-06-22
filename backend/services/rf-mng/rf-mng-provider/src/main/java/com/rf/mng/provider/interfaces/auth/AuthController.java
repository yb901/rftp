package com.rf.mng.provider.interfaces.auth;

import com.rf.mng.provider.application.manager.admin.AdminAuthManager;
import com.rf.mng.provider.application.result.admin.AdminResult;
import com.rf.mng.provider.common.cookie.MngCookie;
import com.rf.mng.provider.common.cookie.MngCookieService;
import com.rf.mng.provider.interfaces.auth.converter.AuthControllerConverter;
import com.rf.mng.provider.interfaces.auth.param.LoginCtrlParam;
import com.rf.mng.provider.interfaces.auth.vo.LoginResultVo;
import com.rf.mng.provider.interfaces.auth.vo.LoginUserVo;
import com.zy.common.core.bo.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理后台认证接口。
 */
@Slf4j
@RestController
@RequestMapping("/mng/auth")
public class AuthController {

    /** 管理员认证应用管理器。 */
    @Resource
    private AdminAuthManager adminAuthManager;

    /** 管理后台 Cookie 服务。 */
    @Resource
    private MngCookieService mngCookieService;

    /**
     * 执行管理后台登录。
     *
     * @param param 登录参数
     * @param response HTTP响应
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResultVo> login(@RequestBody LoginCtrlParam param, HttpServletResponse response) {
        log.info("管理后台登录，username={}", param == null ? null : param.getUsername());
        AdminResult admin = adminAuthManager.login(AuthControllerConverter.toLoginCommand(param));
        mngCookieService.setToResponse(response, AuthControllerConverter.toMngCookie(admin), MngCookie.MNG_COOKIE_NAME);
        return Result.success(AuthControllerConverter.toLoginResultVo(admin));
    }

    /**
     * 退出管理后台登录。
     *
     * @param response HTTP响应
     * @return 空结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        mngCookieService.clearFromResponse(response, MngCookie.MNG_COOKIE_NAME);
        return Result.success();
    }
}
