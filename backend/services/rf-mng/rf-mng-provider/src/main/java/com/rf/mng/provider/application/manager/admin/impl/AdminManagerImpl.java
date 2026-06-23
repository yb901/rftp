package com.rf.mng.provider.application.manager.admin.impl;

import com.rf.mng.provider.application.command.admin.AdminIdCommand;
import com.rf.mng.provider.application.command.admin.AdminSaveCommand;
import com.rf.mng.provider.application.manager.admin.AdminManager;
import com.rf.mng.provider.application.port.persistence.admin.AdminPersistencePort;
import com.rf.mng.provider.application.port.persistence.admin.data.AdminData;
import com.rf.mng.provider.application.port.persistence.admin.record.AdminRecord;
import com.rf.mng.provider.application.query.admin.AdminPageQuery;
import com.rf.mng.provider.application.result.admin.AdminResult;
import com.rf.mng.provider.application.result.admin.AdminTotpResult;
import com.rf.mng.provider.common.auth.AdminRole;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.common.utils.PasswordHashUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员应用管理器实现。
 */
@Service
public class AdminManagerImpl implements AdminManager {

    /** TOTP 密钥字符表。 */
    private static final char[] TOTP_SECRET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    /** TOTP 密钥长度。 */
    private static final int TOTP_SECRET_LENGTH = 32;

    /** 安全随机数生成器。 */
    private final SecureRandom secureRandom = new SecureRandom();

    /** 管理员持久化端口。 */
    @Resource
    private AdminPersistencePort adminPersistencePort;

    /**
     * 分页查询管理员。
     *
     * @param query 查询参数
     * @return 管理员分页
     */
    @Override
    public PageResp<AdminResult> page(AdminPageQuery query) {
        AdminPageQuery safeQuery = safeQuery(query);
        long total = adminPersistencePort.count(safeQuery);
        List<AdminRecord> records = adminPersistencePort.page(safeQuery);
        List<AdminResult> list = records == null ? new ArrayList<>() : records.stream().map(this::toResult).toList();
        return PageResp.of(list, total, safeQuery.getPage(), safeQuery.getSize());
    }

    /**
     * 新增管理员。
     *
     * @param command 保存命令
     * @return 管理员结果
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public AdminResult save(AdminSaveCommand command) {
        validateSave(command, true);
        AdminRecord exists = adminPersistencePort.getByUsername(command.getUsername());
        if (exists != null) {
            throw new BusinessException(ErrorCode.E999001, "用户名已存在");
        }
        AdminData data = toData(command);
        data.setPassword(PasswordHashUtil.hashPassword(command.getPassword()));
        data.setEnabled(command.getEnabled() == null ? 1 : command.getEnabled());
        adminPersistencePort.insert(data);
        return toResult(adminPersistencePort.getById(data.getId()));
    }

    /**
     * 更新管理员。
     *
     * @param command 保存命令
     * @return 管理员结果
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public AdminResult update(AdminSaveCommand command) {
        validateSave(command, false);
        AdminRecord current = getExisting(command.getId());
        AdminData data = toData(command);
        data.setUsername(current.getUsername());
        data.setPassword(StringUtils.isBlank(command.getPassword()) ? current.getPassword() : PasswordHashUtil.hashPassword(command.getPassword()));
        adminPersistencePort.update(data);
        return toResult(adminPersistencePort.getById(command.getId()));
    }

    /**
     * 删除管理员。
     *
     * @param command ID 命令
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void delete(AdminIdCommand command) {
        Long id = requiredId(command);
        getExisting(id);
        adminPersistencePort.deleteById(id);
    }

    /**
     * 生成管理员 TOTP 密钥。
     *
     * @param command ID 命令
     * @return TOTP 密钥结果
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public AdminTotpResult generateTotp(AdminIdCommand command) {
        AdminRecord admin = getExisting(requiredId(command));
        String secret = generateSecret();
        adminPersistencePort.updateOtpSecret(admin.getId(), secret);

        AdminTotpResult result = new AdminTotpResult();
        result.setUsername(admin.getUsername());
        result.setSecret(secret);
        result.setQrCodeUri(buildOtpAuthUri(admin.getUsername(), secret));
        return result;
    }

    /**
     * 禁用管理员 TOTP。
     *
     * @param command ID 命令
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void disableTotp(AdminIdCommand command) {
        Long id = requiredId(command);
        getExisting(id);
        adminPersistencePort.updateOtpSecret(id, null);
    }

    /**
     * 生成 TOTP Base32 密钥。
     *
     * @return TOTP 密钥
     */
    private String generateSecret() {
        StringBuilder builder = new StringBuilder(TOTP_SECRET_LENGTH);
        for (int index = 0; index < TOTP_SECRET_LENGTH; index++) {
            builder.append(TOTP_SECRET_CHARS[secureRandom.nextInt(TOTP_SECRET_CHARS.length)]);
        }
        return builder.toString();
    }

    /**
     * 构造认证器二维码 URI。
     *
     * @param username 用户名
     * @param secret TOTP 密钥
     * @return otpauth URI
     */
    private String buildOtpAuthUri(String username, String secret) {
        String issuer = "rf-mng";
        String label = encode(issuer + ":" + username);
        return "otpauth://totp/" + label + "?secret=" + encode(secret) + "&issuer=" + encode(issuer) + "&algorithm=SHA1&digits=6&period=30";
    }

    /**
     * URL 编码。
     *
     * @param value 原始值
     * @return 编码后文本
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * 查询已存在管理员。
     *
     * @param id 管理员 ID
     * @return 管理员记录
     */
    private AdminRecord getExisting(Long id) {
        AdminRecord admin = adminPersistencePort.getById(id);
        if (admin == null) {
            throw new BusinessException(ErrorCode.E999003, "管理员不存在");
        }
        return admin;
    }

    /**
     * 校验 ID 命令。
     *
     * @param command ID 命令
     * @return 管理员 ID
     */
    private Long requiredId(AdminIdCommand command) {
        if (command == null || command.getId() == null) {
            throw new BusinessException(ErrorCode.E999001, "管理员 ID 不能为空");
        }
        return command.getId();
    }

    /**
     * 校验保存命令。
     *
     * @param command 保存命令
     * @param create 是否新增
     */
    private void validateSave(AdminSaveCommand command, boolean create) {
        if (command == null) {
            throw new BusinessException(ErrorCode.E999001, "管理员参数不能为空");
        }
        if (!create && command.getId() == null) {
            throw new BusinessException(ErrorCode.E999001, "管理员 ID 不能为空");
        }
        if (create && StringUtils.isBlank(command.getUsername())) {
            throw new BusinessException(ErrorCode.E999001, "用户名不能为空");
        }
        if (create && StringUtils.isBlank(command.getPassword())) {
            throw new BusinessException(ErrorCode.E999001, "密码不能为空");
        }
        if (StringUtils.isBlank(command.getRealName())) {
            throw new BusinessException(ErrorCode.E999001, "姓名不能为空");
        }
        if (command.getRole() == null) {
            throw new BusinessException(ErrorCode.E999001, "角色不能为空");
        }
        if (!AdminRole.valid(command.getRole())) {
            throw new BusinessException(ErrorCode.E999001, "角色不合法");
        }
    }

    /**
     * 生成安全分页查询。
     *
     * @param query 原始查询
     * @return 查询参数
     */
    private AdminPageQuery safeQuery(AdminPageQuery query) {
        AdminPageQuery safeQuery = query == null ? new AdminPageQuery() : query;
        if (safeQuery.getPage() == null || safeQuery.getPage() < 1) {
            safeQuery.setPage(1);
        }
        if (safeQuery.getSize() == null || safeQuery.getSize() < 1) {
            safeQuery.setSize(10);
        }
        return safeQuery;
    }

    /**
     * 转换管理员写入数据。
     *
     * @param command 保存命令
     * @return 写入数据
     */
    private AdminData toData(AdminSaveCommand command) {
        AdminData data = new AdminData();
        data.setId(command.getId());
        data.setUsername(StringUtils.trim(command.getUsername()));
        data.setRealName(StringUtils.trim(command.getRealName()));
        data.setEnabled(command.getEnabled());
        data.setRole(command.getRole());
        return data;
    }

    /**
     * 转换管理员应用结果。
     *
     * @param record 管理员记录
     * @return 管理员结果
     */
    private AdminResult toResult(AdminRecord record) {
        if (record == null) {
            return null;
        }
        AdminResult result = new AdminResult();
        result.setId(record.getId());
        result.setUsername(record.getUsername());
        result.setRealName(record.getRealName());
        result.setEnabled(record.getEnabled());
        result.setRole(record.getRole());
        result.setTotpEnabled(StringUtils.isNotBlank(record.getOtpSecret()));
        result.setGmtCreate(record.getGmtCreate());
        result.setGmtModified(record.getGmtModified());
        return result;
    }
}
