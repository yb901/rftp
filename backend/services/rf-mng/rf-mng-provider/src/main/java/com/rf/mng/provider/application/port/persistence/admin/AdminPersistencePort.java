package com.rf.mng.provider.application.port.persistence.admin;

import com.rf.mng.provider.application.port.persistence.admin.record.AdminRecord;

/**
 * 管理员持久化端口。
 */
public interface AdminPersistencePort {

    /**
     * 按用户名查询管理员。
     *
     * @param username 用户名
     * @return 管理员记录，不存在时返回空
     */
    AdminRecord getByUsername(String username);
}
