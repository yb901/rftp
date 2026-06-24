package com.rf.performance.provider.interfaces.controller.employee;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.command.performance.employee.EmployeePerformanceLoginCommand;
import com.rf.performance.provider.application.command.performance.employee.EmployeePerformanceSmsSendCommand;
import com.rf.performance.provider.application.manager.performance.employee.EmployeePerformanceClientManager;
import com.rf.performance.provider.application.result.performance.employee.EmployeePerformanceLoginResult;
import com.rf.performance.provider.common.config.PerformanceSmsProperties;
import com.rf.performance.provider.common.config.PerformanceWebAuthProperties;
import com.rf.performance.provider.common.web.EmployeePerformanceRequestContext;
import com.rf.performance.provider.interfaces.controller.employee.param.EmployeePerformanceLoginCtrlParam;
import com.rf.performance.provider.interfaces.controller.employee.param.EmployeePerformanceMobileCheckCtrlParam;
import com.rf.performance.provider.interfaces.controller.employee.param.EmployeePerformanceSmsSendCtrlParam;
import com.rf.performance.provider.interfaces.controller.employee.vo.auth.EmployeePerformanceLoginVo;
import com.rf.performance.provider.interfaces.controller.employee.vo.auth.EmployeePerformancePendingCheckVo;
import com.rf.performance.provider.interfaces.controller.employee.vo.auth.PerformanceCaptchaConfigVo;
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
 * 员工端绩效登录接口。
 */
@RestController
@RequestMapping("/performance/employee/auth")
public class EmployeePerformanceAuthController {

    /**
     * 员工端绩效应用编排。
     */
    @Resource
    private EmployeePerformanceClientManager employeePerformanceClientManager;

    /**
     * 员工端 Web 登录配置。
     */
    @Resource
    private PerformanceWebAuthProperties performanceWebAuthProperties;

    /**
     * 短信与验证码配置。
     */
    @Resource
    private PerformanceSmsProperties performanceSmsProperties;

    /**
     * 员工端绩效请求上下文。
     */
    @Resource
    private EmployeePerformanceRequestContext employeePerformanceRequestContext;

    /**
     * 发送短信验证码。
     *
     * @param param 短信发送参数
     * @param request HTTP 请求
     * @return 短信验证留痕 ID
     */
    @PostMapping("/sms/send")
    public Result<Long> sendSmsCode(@RequestBody EmployeePerformanceSmsSendCtrlParam param,
                                    HttpServletRequest request) {
        EmployeePerformanceSmsSendCtrlParam safeParam = param == null ? new EmployeePerformanceSmsSendCtrlParam() : param;
        EmployeePerformanceSmsSendCommand command = BeanUtil.copyProperties(safeParam, EmployeePerformanceSmsSendCommand.class);
        command.setIpAddress(employeePerformanceRequestContext.clientIp(request));
        command.setUserAgent(employeePerformanceRequestContext.userAgent(request));
        return Result.success(employeePerformanceClientManager.sendSmsCode(command));
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
    public Result<EmployeePerformanceLoginVo> login(@RequestBody EmployeePerformanceLoginCtrlParam param,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        EmployeePerformanceLoginCtrlParam safeParam = param == null ? new EmployeePerformanceLoginCtrlParam() : param;
        EmployeePerformanceLoginCommand command = BeanUtil.copyProperties(safeParam, EmployeePerformanceLoginCommand.class);
        command.setIpAddress(employeePerformanceRequestContext.clientIp(request));
        command.setUserAgent(employeePerformanceRequestContext.userAgent(request));
        EmployeePerformanceLoginResult result = employeePerformanceClientManager.login(command);
        setLoginCookie(response, result.getMobile());
        return Result.success(BeanUtil.copyProperties(result, EmployeePerformanceLoginVo.class));
    }

    /**
     * 查询当前登录态。
     *
     * @param request HTTP 请求
     * @return 登录结果
     */
    @GetMapping("/me")
    public Result<EmployeePerformanceLoginVo> me(HttpServletRequest request) {
        EmployeePerformanceLoginVo vo = new EmployeePerformanceLoginVo();
        vo.setMobile(employeePerformanceRequestContext.requireMobile(request));
        return Result.success(vo);
    }

    /**
     * 登录前检查手机号是否有待处理绩效。
     *
     * @param param 手机号检查参数
     * @return 待处理绩效检查结果
     */
    @PostMapping("/performance/check")
    public Result<EmployeePerformancePendingCheckVo> checkPendingPerformance(@RequestBody EmployeePerformanceMobileCheckCtrlParam param) {
        EmployeePerformanceMobileCheckCtrlParam safeParam = param == null ? new EmployeePerformanceMobileCheckCtrlParam() : param;
        boolean hasPendingPerformance = employeePerformanceClientManager.hasPendingPerformance(safeParam.getMobile());
        EmployeePerformancePendingCheckVo vo = new EmployeePerformancePendingCheckVo();
        vo.setHasPendingPerformance(hasPendingPerformance);
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
        vo.setEnabled(performanceSmsProperties.getCaptchaEnabled());
        vo.setRegion(performanceSmsProperties.getCaptchaRegion());
        vo.setPrefix(performanceSmsProperties.getCaptchaPrefix());
        vo.setSceneId(performanceSmsProperties.getCaptchaSceneId());
        vo.setLanguage(performanceSmsProperties.getCaptchaLanguage());
        vo.setJsUrl(performanceSmsProperties.getCaptchaJsUrl());
        return Result.success(vo);
    }

    /**
     * 写入登录 Cookie。
     *
     * @param response HTTP 响应
     * @param mobile 手机号
     */
    private void setLoginCookie(HttpServletResponse response, String mobile) {
        Cookie cookie = new Cookie(PerformanceWebAuthProperties.COOKIE_NAME, employeePerformanceRequestContext.createLoginToken(mobile));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(performanceWebAuthProperties.getCookieMaxAgeSeconds());
        response.addCookie(cookie);
    }
}
