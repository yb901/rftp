package com.rf.mng.provider.interfaces.auth.converter;

import com.rf.mng.provider.application.command.admin.AdminLoginCommand;
import com.rf.mng.provider.application.result.admin.AdminResult;
import com.rf.mng.provider.common.cookie.MngCookie;
import com.rf.mng.provider.interfaces.auth.param.LoginCtrlParam;
import com.rf.mng.provider.interfaces.auth.vo.LoginResultVo;
import com.rf.mng.provider.interfaces.auth.vo.LoginUserVo;

/**
 * 管理后台认证 HTTP 对象转换器。
 */
public final class AuthControllerConverter {

    /** 私有构造方法。 */
    private AuthControllerConverter() {
    }

    /**
     * 转换登录命令。
     *
     * @param param 登录参数
     * @return 登录命令
     */
    public static AdminLoginCommand toLoginCommand(LoginCtrlParam param) {
        AdminLoginCommand command = new AdminLoginCommand();
        if (param == null) {
            return command;
        }
        command.setUsername(param.getUsername());
        command.setPassword(param.getPassword());
        return command;
    }

    /**
     * 转换登录返回视图。
     *
     * @param result 管理员结果
     * @return 登录返回视图
     */
    public static LoginResultVo toLoginResultVo(AdminResult result) {
        LoginResultVo vo = new LoginResultVo();
        vo.setToken(null);
        vo.setUser(toLoginUserVo(result));
        return vo;
    }

    /**
     * 转换登录用户视图。
     *
     * @param result 管理员结果
     * @return 登录用户视图
     */
    public static LoginUserVo toLoginUserVo(AdminResult result) {
        if (result == null) {
            return null;
        }
        LoginUserVo vo = new LoginUserVo();
        vo.setId(result.getId());
        vo.setUsername(result.getUsername());
        vo.setRealName(result.getRealName());
        vo.setRole(result.getRole());
        return vo;
    }

    /**
     * 转换管理后台 Cookie。
     *
     * @param result 管理员结果
     * @return Cookie对象
     */
    public static MngCookie toMngCookie(AdminResult result) {
        MngCookie cookie = new MngCookie();
        cookie.setAdminId(result.getId());
        cookie.setUsername(result.getUsername());
        cookie.setRealName(result.getRealName());
        cookie.setRole(String.valueOf(result.getRole()));
        return cookie;
    }
}
