package com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.impl;

import com.zy.rfpt.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentBatchPersistencePort;
import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.mapper.SocialSecurityPaymentBatchMapper;
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
    public int insert(SocialSecurityPaymentBatchEntity entity) {
        return mapper.insert(entity);
    }

    @Override
    public long count(SocialSecurityPaymentBatchQuery query) {
        return mapper.count(query);
    }

    @Override
    public List<SocialSecurityPaymentBatchEntity> page(SocialSecurityPaymentBatchQuery query) {
        int offset = Math.max(query.getPage() - 1, 0) * query.getSize();
        return mapper.page(query, offset, query.getSize());
    }
}
