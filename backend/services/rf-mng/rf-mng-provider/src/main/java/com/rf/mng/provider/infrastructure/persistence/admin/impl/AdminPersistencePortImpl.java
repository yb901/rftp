package com.rf.mng.provider.infrastructure.persistence.admin.impl;

import com.rf.mng.provider.application.port.persistence.admin.AdminPersistencePort;
import com.rf.mng.provider.application.port.persistence.admin.record.AdminRecord;
import com.rf.mng.provider.infrastructure.persistence.admin.entity.AdminEntity;
import com.rf.mng.provider.infrastructure.persistence.platform.admin.mapper.AdminMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 管理员持久化端口实现。
 */
@Repository
public class AdminPersistencePortImpl implements AdminPersistencePort {

    /** 管理员 Mapper。 */
    @Resource
    private AdminMapper adminMapper;

    /**
     * 按用户名查询管理员。
     *
     * @param username 用户名
     * @return 管理员记录，不存在时返回空
     */
    @Override
    public AdminRecord getByUsername(String username) {
        return toRecord(adminMapper.selectByUsername(username));
    }

    /**
     * 转换管理员读取记录。
     *
     * @param entity 管理员实体
     * @return 管理员记录
     */
    private AdminRecord toRecord(AdminEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminRecord record = new AdminRecord();
        record.setId(entity.getId());
        record.setUsername(entity.getUsername());
        record.setRealName(entity.getRealName());
        record.setPassword(entity.getPassword());
        record.setOtpSecret(entity.getOtpSecret());
        record.setEnabled(entity.getEnabled());
        record.setRole(entity.getRole());
        record.setGmtCreate(entity.getGmtCreate());
        record.setGmtModified(entity.getGmtModified());
        return record;
    }
}
