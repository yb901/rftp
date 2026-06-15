package com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.impl;

import com.zy.rfpt.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentTaskPersistencePort;
import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.mapper.SocialSecurityPaymentTaskMapper;
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
    public int batchInsert(List<SocialSecurityPaymentTaskEntity> entities) {
        return mapper.batchInsert(entities);
    }

    @Override
    public SocialSecurityPaymentTaskEntity findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public int markRetry(Long id, String operator) {
        return mapper.markRetry(id, operator);
    }

    @Override
    public long count(SocialSecurityPaymentTaskQuery query) {
        return mapper.count(query);
    }

    @Override
    public List<SocialSecurityPaymentTaskEntity> page(SocialSecurityPaymentTaskQuery query) {
        int offset = Math.max(query.getPage() - 1, 0) * query.getSize();
        return mapper.page(query, offset, query.getSize());
    }
}
