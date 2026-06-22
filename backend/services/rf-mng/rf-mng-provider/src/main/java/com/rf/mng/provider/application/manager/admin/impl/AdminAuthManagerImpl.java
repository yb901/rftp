package com.rf.mng.provider.application.manager.admin.impl;

import com.rf.mng.provider.application.command.admin.AdminLoginCommand;
import com.rf.mng.provider.application.manager.admin.AdminAuthManager;
import com.rf.mng.provider.application.port.persistence.admin.AdminPersistencePort;
import com.rf.mng.provider.application.port.persistence.admin.record.AdminRecord;
import com.rf.mng.provider.application.result.admin.AdminResult;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.common.utils.PasswordHashUtil;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 管理员认证应用管理器实现。
 */
@Slf4j
@Service
public class AdminAuthManagerImpl implements AdminAuthManager {

    /** 管理员持久化端口。 */
    @Resource
    private AdminPersistencePort adminPersistencePort;

    /** OTP动态验证码校验器。 */
    private final DefaultCodeVerifier otpCodeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    /**
     * 执行管理员登录。
     *
     * @param command 登录命令
     * @return 登录管理员信息
     */
    @Override
    public AdminResult login(AdminLoginCommand command) {
        validateLoginCommand(command);
        AdminRecord admin = adminPersistencePort.getByUsername(command.getUsername());
        if (admin == null) {
            log.warn("管理员登录失败，用户不存在，username={}", command.getUsername());
            throw new BusinessException(ErrorCode.E100002, "账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(admin.getEnabled())) {
            log.warn("管理员登录失败，用户已禁用，username={}, adminId={}", command.getUsername(), admin.getId());
            throw new BusinessException(ErrorCode.E100002, "账号或密码错误");
        }
        verifyPassword(command, admin);
        verifyOtp(command, admin);
        log.info("管理员登录成功，username={}, adminId={}", command.getUsername(), admin.getId());
        return toResult(admin);
    }

    /**
     * 校验登录命令。
     *
     * @param command 登录命令
     */
    private void validateLoginCommand(AdminLoginCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.E999001, "登录参数不能为空");
        }
        if (StringUtils.isBlank(command.getUsername())) {
            throw new BusinessException(ErrorCode.E999001, "用户名不能为空");
        }
        if (StringUtils.isBlank(command.getPassword())) {
            throw new BusinessException(ErrorCode.E999001, "密码不能为空");
        }
    }

    /**
     * 校验登录密码。
     *
     * @param command 登录命令
     * @param admin 管理员记录
     */
    private void verifyPassword(AdminLoginCommand command, AdminRecord admin) {
        if (!PasswordHashUtil.verifyPassword(command.getPassword(), admin.getPassword())) {
            log.warn("管理员登录失败，密码错误，username={}", command.getUsername());
            throw new BusinessException(ErrorCode.E100002, "账号或密码错误");
        }
    }

    /**
     * 校验OTP动态验证码。
     *
     * @param command 登录命令
     * @param admin 管理员记录
     */
    private void verifyOtp(AdminLoginCommand command, AdminRecord admin) {
        if (StringUtils.isBlank(admin.getOtpSecret())) {
            return;
        }
        String otpCode = StringUtils.deleteWhitespace(command.getOtpCode());
        if (StringUtils.isBlank(otpCode)) {
            log.warn("管理员登录失败，未填写OTP验证码，username={}, adminId={}", command.getUsername(), admin.getId());
            throw new BusinessException(ErrorCode.E999001, "请输入动态验证码");
        }
        if (!StringUtils.isNumeric(otpCode) || !otpCodeVerifier.isValidCode(admin.getOtpSecret(), otpCode)) {
            log.warn("管理员登录失败，OTP验证码错误，username={}, adminId={}", command.getUsername(), admin.getId());
            throw new BusinessException(ErrorCode.E100002, "动态验证码错误");
        }
    }

    /**
     * 转换管理员登录结果。
     *
     * @param admin 管理员记录
     * @return 登录结果
     */
    private AdminResult toResult(AdminRecord admin) {
        AdminResult result = new AdminResult();
        result.setId(admin.getId());
        result.setUsername(admin.getUsername());
        result.setRealName(admin.getRealName());
        result.setEnabled(admin.getEnabled());
        result.setRole(admin.getRole());
        result.setGmtCreate(admin.getGmtCreate());
        result.setGmtModified(admin.getGmtModified());
        return result;
    }
}
