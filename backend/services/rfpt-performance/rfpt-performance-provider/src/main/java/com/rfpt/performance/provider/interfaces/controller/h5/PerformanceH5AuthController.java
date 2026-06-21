package com.rfpt.performance.provider.interfaces.controller.h5;

import cn.hutool.core.bean.BeanUtil;
import com.rfpt.performance.provider.application.command.performance.h5.PerformanceH5LoginCommand;
import com.rfpt.performance.provider.application.command.performance.h5.PerformanceH5SmsSendCommand;
import com.rfpt.performance.provider.application.manager.performance.h5.EmployeePerformanceH5Manager;
import com.rfpt.performance.provider.application.result.performance.h5.PerformanceH5LoginResult;
import com.rfpt.performance.provider.common.config.PerformanceH5AuthProperties;
import com.rfpt.performance.provider.common.web.PerformanceH5RequestContext;
import com.rfpt.performance.provider.interfaces.controller.h5.param.PerformanceH5LoginCtrlParam;
import com.rfpt.performance.provider.interfaces.controller.h5.param.PerformanceH5SmsSendCtrlParam;
import com.rfpt.performance.provider.interfaces.controller.h5.vo.auth.PerformanceCaptchaConfigVo;
import com.rfpt.performance.provider.interfaces.controller.h5.vo.auth.PerformanceH5LoginVo;
import com.zy.common.core.bo.Result;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 员工绩效 H5 登录接口。
 */
@RestController
@RequestMapping("/api/performance/h5/auth")
public class PerformanceH5AuthController {

    /**
     * 员工绩效 H5 应用编排。
     */
    @Resource
    private EmployeePerformanceH5Manager employeePerformanceH5Manager;

    /**
     * H5 登录配置。
     */
    @Resource
    private PerformanceH5AuthProperties performanceH5AuthProperties;

    /**
     * 员工绩效 H5 请求上下文。
     */
    @Resource
    private PerformanceH5RequestContext performanceH5RequestContext;

    /**
     * 发送短信验证码。
     *
     * @param param 短信发送参数
     * @param request HTTP 请求
     * @return 短信验证留痕 ID
     */
    @PostMapping("/sms/send")
    public Result<Long> sendSmsCode(@RequestBody PerformanceH5SmsSendCtrlParam param,
                                    HttpServletRequest request) {
        PerformanceH5SmsSendCtrlParam safeParam = param == null ? new PerformanceH5SmsSendCtrlParam() : param;
        PerformanceH5SmsSendCommand command = BeanUtil.copyProperties(safeParam, PerformanceH5SmsSendCommand.class);
        command.setIpAddress(performanceH5RequestContext.clientIp(request));
        command.setUserAgent(performanceH5RequestContext.userAgent(request));
        return Result.success(employeePerformanceH5Manager.sendSmsCode(command));
    }

    /**
     * 手机号登录。
     *
     * @param param 登录参数
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<PerformanceH5LoginVo> login(@RequestBody PerformanceH5LoginCtrlParam param,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        PerformanceH5LoginCtrlParam safeParam = param == null ? new PerformanceH5LoginCtrlParam() : param;
        PerformanceH5LoginCommand command = BeanUtil.copyProperties(safeParam, PerformanceH5LoginCommand.class);
        command.setIpAddress(performanceH5RequestContext.clientIp(request));
        command.setUserAgent(performanceH5RequestContext.userAgent(request));
        PerformanceH5LoginResult result = employeePerformanceH5Manager.login(command);
        setLoginCookie(response, result.getMobile());
        return Result.success(BeanUtil.copyProperties(result, PerformanceH5LoginVo.class));
    }

    /**
     * 查询当前登录态。
     *
     * @param request HTTP 请求
     * @return 登录结果
     */
    @GetMapping("/me")
    public Result<PerformanceH5LoginVo> me(HttpServletRequest request) {
        PerformanceH5LoginVo vo = new PerformanceH5LoginVo();
        vo.setMobile(performanceH5RequestContext.requireMobile(request));
        return Result.success(vo);
    }

    /**
     * 查询图形验证码前端配置。
     *
     * @return 图形验证码前端配置
     */
    @GetMapping("/captcha/config")
    public Result<PerformanceCaptchaConfigVo> captchaConfig() {
        PerformanceCaptchaConfigVo vo = new PerformanceCaptchaConfigVo();
        vo.setEnabled(performanceH5AuthProperties.getCaptchaEnabled());
        vo.setRegion(performanceH5AuthProperties.getCaptchaRegion());
        vo.setPrefix(performanceH5AuthProperties.getCaptchaPrefix());
        vo.setSceneId(performanceH5AuthProperties.getCaptchaSceneId());
        vo.setLanguage(performanceH5AuthProperties.getCaptchaLanguage());
        vo.setJsUrl(performanceH5AuthProperties.getCaptchaJsUrl());
        return Result.success(vo);
    }

    /**
     * 写入登录 Cookie。
     *
     * @param response HTTP 响应
     * @param mobile 手机号
     */
    private void setLoginCookie(HttpServletResponse response, String mobile) {
        Cookie cookie = new Cookie(performanceH5AuthProperties.getCookieName(), mobile);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(performanceH5AuthProperties.getCookieMaxAgeSeconds());
        response.addCookie(cookie);
    }
}
