package com.rf.mng.provider.infrastructure.persistence.socialsecurity.impl;

import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentBatchPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentBatchData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentBatchRecord;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;
import com.rf.mng.provider.infrastructure.persistence.platform.socialsecurity.mapper.SocialSecurityPaymentBatchMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 社保缴费批次持久化实现。
 */
@Repository
public class SocialSecurityPaymentBatchPersistencePortImpl implements SocialSecurityPaymentBatchPersistencePort {

    /** 批次 Mapper。 */
    @Resource
    private SocialSecurityPaymentBatchMapper mapper;

    @Override
    public int insert(SocialSecurityPaymentBatchData data) {
        SocialSecurityPaymentBatchEntity entity = toEntity(data);
        int result = mapper.insert(entity);
        data.setId(entity.getId());
        return result;
    }

    @Override
    public long count(SocialSecurityPaymentBatchQuery query) {
        return mapper.count(query);
    }

    @Override
    public List<SocialSecurityPaymentBatchRecord> page(SocialSecurityPaymentBatchQuery query) {
        int offset = Math.max(query.getPage() - 1, 0) * query.getSize();
        List<SocialSecurityPaymentBatchEntity> entities = mapper.page(query, offset, query.getSize());
        return entities.stream().map(this::toRecord).toList();
    }

    private SocialSecurityPaymentBatchEntity toEntity(SocialSecurityPaymentBatchData data) {
        SocialSecurityPaymentBatchEntity entity = new SocialSecurityPaymentBatchEntity();
        entity.setId(data.getId());
        entity.setRegionCode(data.getRegionCode());
        entity.setSiteType(data.getSiteType());
        entity.setPeriodMonth(data.getPeriodMonth());
        entity.setStatus(data.getStatus());
        entity.setTotalCount(data.getTotalCount());
        entity.setSuccessCount(data.getSuccessCount());
        entity.setFailedCount(data.getFailedCount());
        entity.setCreateAdminId(data.getCreateAdminId());
        entity.setCreateAdminName(data.getCreateAdminName());
        return entity;
    }

    private SocialSecurityPaymentBatchRecord toRecord(SocialSecurityPaymentBatchEntity entity) {
        SocialSecurityPaymentBatchRecord record = new SocialSecurityPaymentBatchRecord();
        record.setId(entity.getId());
        record.setRegionCode(entity.getRegionCode());
        record.setRegionName(entity.getRegionName());
        record.setSiteType(entity.getSiteType());
        record.setPeriodMonth(entity.getPeriodMonth());
        record.setStatus(entity.getStatus());
        record.setTotalCount(entity.getTotalCount());
        record.setSuccessCount(entity.getSuccessCount());
        record.setFailedCount(entity.getFailedCount());
        record.setCreateAdminId(entity.getCreateAdminId());
        record.setCreateAdminName(entity.getCreateAdminName());
        record.setGmtCreate(entity.getGmtCreate());
        record.setGmtModified(entity.getGmtModified());
        return record;
    }
}
