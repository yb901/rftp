package com.rf.mng.provider.infrastructure.persistence.socialsecurity.impl;

import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentTaskPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentTaskData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentTaskRecord;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;
import com.rf.mng.provider.infrastructure.persistence.robot.socialsecurity.mapper.SocialSecurityPaymentTaskMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 社保缴费任务持久化实现。
 */
@Repository
public class SocialSecurityPaymentTaskPersistencePortImpl implements SocialSecurityPaymentTaskPersistencePort {

    /** 任务 Mapper。 */
    @Resource
    private SocialSecurityPaymentTaskMapper mapper;

    @Override
    public int batchInsert(List<SocialSecurityPaymentTaskData> records) {
        List<SocialSecurityPaymentTaskEntity> entities = records.stream().map(this::toEntity).toList();
        return mapper.batchInsert(entities);
    }

    @Override
    public SocialSecurityPaymentTaskRecord findById(Long id) {
        SocialSecurityPaymentTaskEntity entity = mapper.findById(id);
        if (entity == null) {
            return null;
        }
        return toRecord(entity);
    }

    @Override
    public int markRetry(Long id) {
        return mapper.markRetry(id);
    }

    @Override
    public long count(SocialSecurityPaymentTaskQuery query) {
        return mapper.count(query);
    }

    @Override
    public List<SocialSecurityPaymentTaskRecord> page(SocialSecurityPaymentTaskQuery query) {
        int offset = Math.max(query.getPage() - 1, 0) * query.getSize();
        List<SocialSecurityPaymentTaskEntity> entities = mapper.page(query, offset, query.getSize());
        return entities.stream().map(this::toRecord).toList();
    }

    private SocialSecurityPaymentTaskEntity toEntity(SocialSecurityPaymentTaskData data) {
        SocialSecurityPaymentTaskEntity entity = new SocialSecurityPaymentTaskEntity();
        entity.setId(data.getId());
        entity.setBatchId(data.getBatchId());
        entity.setTaxNo(data.getTaxNo());
        entity.setRegionCode(data.getRegionCode());
        entity.setSiteType(data.getSiteType());
        entity.setPeriodMonth(data.getPeriodMonth());
        entity.setStatus(data.getStatus());
        entity.setRetryable(data.getRetryable());
        entity.setRetryCount(data.getRetryCount());
        entity.setMaxRetryCount(data.getMaxRetryCount());
        entity.setCreateAdminId(data.getCreateAdminId());
        entity.setCreateAdminName(data.getCreateAdminName());
        return entity;
    }

    private SocialSecurityPaymentTaskRecord toRecord(SocialSecurityPaymentTaskEntity entity) {
        SocialSecurityPaymentTaskRecord record = new SocialSecurityPaymentTaskRecord();
        record.setId(entity.getId());
        record.setBatchId(entity.getBatchId());
        record.setTaxNo(entity.getTaxNo());
        record.setEnterpriseName(entity.getEnterpriseName());
        record.setSecurityAccountName(entity.getSecurityAccountName());
        record.setRegionCode(entity.getRegionCode());
        record.setSiteType(entity.getSiteType());
        record.setPeriodMonth(entity.getPeriodMonth());
        record.setStatus(entity.getStatus());
        record.setPayableAmount(entity.getPayableAmount());
        record.setErrorCode(entity.getErrorCode());
        record.setErrorMessage(entity.getErrorMessage());
        record.setRetryable(entity.getRetryable());
        record.setRetryCount(entity.getRetryCount());
        record.setMaxRetryCount(entity.getMaxRetryCount());
        record.setCreateAdminId(entity.getCreateAdminId());
        record.setCreateAdminName(entity.getCreateAdminName());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        return record;
    }
}
