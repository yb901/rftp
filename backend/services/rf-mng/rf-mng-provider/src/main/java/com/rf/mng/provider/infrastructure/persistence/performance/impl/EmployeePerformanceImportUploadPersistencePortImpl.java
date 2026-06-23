package com.rf.mng.provider.infrastructure.persistence.performance.impl;

import com.rf.mng.provider.application.port.persistence.performance.EmployeePerformanceImportUploadPersistencePort;
import com.rf.mng.provider.application.port.persistence.performance.data.EmployeePerformanceImportUploadData;
import com.rf.mng.provider.application.port.persistence.performance.record.EmployeePerformanceImportUploadRecord;
import com.rf.mng.provider.infrastructure.persistence.performance.entity.EmployeePerformanceImportUploadEntity;
import com.rf.mng.provider.infrastructure.persistence.platform.performance.mapper.EmployeePerformanceImportUploadMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 员工绩效导入上传记录持久化实现。
 */
@Repository
public class EmployeePerformanceImportUploadPersistencePortImpl implements EmployeePerformanceImportUploadPersistencePort {

    /** 员工绩效导入上传记录 Mapper。 */
    @Resource
    private EmployeePerformanceImportUploadMapper mapper;

    /**
     * 新增上传记录。
     *
     * @param data 上传记录写入数据
     * @return 影响行数
     */
    @Override
    public int insert(EmployeePerformanceImportUploadData data) {
        EmployeePerformanceImportUploadEntity entity = toEntity(data);
        int result = mapper.insert(entity);
        data.setId(entity.getId());
        return result;
    }

    /**
     * 按任务查询最近上传记录。
     *
     * @param taskId 绩效任务ID，可为空
     * @param limit 查询数量
     * @return 上传记录列表
     */
    @Override
    public List<EmployeePerformanceImportUploadRecord> listRecent(Long taskId, int limit) {
        return mapper.listRecent(taskId, Math.max(1, limit)).stream().map(this::toRecord).toList();
    }

    /**
     * 按ID查询上传记录。
     *
     * @param id 上传记录ID
     * @return 上传记录
     */
    @Override
    public EmployeePerformanceImportUploadRecord getById(Long id) {
        EmployeePerformanceImportUploadEntity entity = mapper.getById(id);
        if (entity == null) {
            return null;
        }
        return toRecord(entity);
    }

    /**
     * 转换写入实体。
     *
     * @param data 写入数据
     * @return 数据库实体
     */
    private EmployeePerformanceImportUploadEntity toEntity(EmployeePerformanceImportUploadData data) {
        EmployeePerformanceImportUploadEntity entity = new EmployeePerformanceImportUploadEntity();
        entity.setId(data.getId());
        entity.setTaskId(data.getTaskId());
        entity.setTaskName(data.getTaskName());
        entity.setFileName(data.getFileName());
        entity.setOriginalContentType(data.getOriginalContentType());
        entity.setOriginalFileUrl(data.getOriginalFileUrl());
        entity.setFailureFileName(data.getFailureFileName());
        entity.setFailureFileUrl(data.getFailureFileUrl());
        entity.setTotalCount(data.getTotalCount());
        entity.setSuccessCount(data.getSuccessCount());
        entity.setFailCount(data.getFailCount());
        entity.setStatus(data.getStatus());
        entity.setErrorMessage(data.getErrorMessage());
        entity.setCreateAdminId(data.getCreateAdminId());
        entity.setCreateAdminName(data.getCreateAdminName());
        return entity;
    }

    /**
     * 转换读取结果。
     *
     * @param entity 数据库实体
     * @return 应用层记录
     */
    private EmployeePerformanceImportUploadRecord toRecord(EmployeePerformanceImportUploadEntity entity) {
        EmployeePerformanceImportUploadRecord record = new EmployeePerformanceImportUploadRecord();
        record.setId(entity.getId());
        record.setTaskId(entity.getTaskId());
        record.setTaskName(entity.getTaskName());
        record.setFileName(entity.getFileName());
        record.setOriginalContentType(entity.getOriginalContentType());
        record.setOriginalFileUrl(entity.getOriginalFileUrl());
        record.setFailureFileName(entity.getFailureFileName());
        record.setFailureFileUrl(entity.getFailureFileUrl());
        record.setTotalCount(entity.getTotalCount());
        record.setSuccessCount(entity.getSuccessCount());
        record.setFailCount(entity.getFailCount());
        record.setStatus(entity.getStatus());
        record.setErrorMessage(entity.getErrorMessage());
        record.setCreateAdminId(entity.getCreateAdminId());
        record.setCreateAdminName(entity.getCreateAdminName());
        record.setGmtCreate(entity.getGmtCreate());
        return record;
    }
}
