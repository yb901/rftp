package com.rf.mng.provider.application.manager.admin;

import com.rf.mng.provider.application.command.admin.AdminLoginCommand;
import com.rf.mng.provider.application.result.admin.AdminResult;

/**
 * 管理员认证应用管理器。
 */
public interface AdminAuthManager {

    /**
     * 执行管理员登录。
     *
     * @param command 登录命令
     * @return 登录管理员信息
     */
    AdminResult login(AdminLoginCommand command);
}
