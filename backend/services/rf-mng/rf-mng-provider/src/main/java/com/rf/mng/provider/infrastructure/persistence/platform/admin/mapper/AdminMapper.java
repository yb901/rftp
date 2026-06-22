package com.rf.mng.provider.infrastructure.persistence.platform.admin.mapper;

import com.rf.mng.provider.infrastructure.persistence.admin.entity.AdminEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员 Mapper。
 */
public interface AdminMapper {

    /**
     * 按用户名查询管理员。
     *
     * @param username 用户名
     * @return 管理员实体
     */
    AdminEntity selectByUsername(@Param("username") String username);
}
