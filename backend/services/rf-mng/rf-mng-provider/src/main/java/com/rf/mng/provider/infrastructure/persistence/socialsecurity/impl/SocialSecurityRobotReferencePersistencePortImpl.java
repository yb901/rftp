package com.rf.mng.provider.infrastructure.persistence.socialsecurity.impl;

import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityRobotReferencePersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentBatchRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentTaskRecord;
import com.rf.mng.provider.infrastructure.persistence.robot.socialsecurity.mapper.SocialSecurityRobotReferenceMapper;
import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;
import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 社保缴费机器人基础信息持久化实现。
 */
@Repository
public class SocialSecurityRobotReferencePersistencePortImpl implements SocialSecurityRobotReferencePersistencePort {

    /** 机器人基础信息 Mapper。 */
    @Resource
    private SocialSecurityRobotReferenceMapper mapper;

    @Override
    public List<SocialSecurityPaymentBatchRecord> listRegionNamesByRegionCodes(List<String> regionCodes) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return List.of();
        }
        List<SocialSecurityPaymentBatchEntity> entities = mapper.listRegionNamesByRegionCodes(regionCodes);
        return entities.stream().map(this::toBatchRecord).toList();
    }

    @Override
    public List<SocialSecurityPaymentTaskRecord> listEnterpriseInfoByTaxNos(List<String> taxNos) {
        if (taxNos == null || taxNos.isEmpty()) {
            return List.of();
        }
        List<SocialSecurityPaymentTaskEntity> entities = mapper.listEnterpriseInfoByTaxNos(taxNos);
        return entities.stream().map(this::toTaskRecord).toList();
    }

    private SocialSecurityPaymentBatchRecord toBatchRecord(SocialSecurityPaymentBatchEntity entity) {
        SocialSecurityPaymentBatchRecord record = new SocialSecurityPaymentBatchRecord();
        record.setRegionCode(entity.getRegionCode());
        record.setRegionName(entity.getRegionName());
        return record;
    }

    private SocialSecurityPaymentTaskRecord toTaskRecord(SocialSecurityPaymentTaskEntity entity) {
        SocialSecurityPaymentTaskRecord record = new SocialSecurityPaymentTaskRecord();
        record.setTaxNo(entity.getTaxNo());
        record.setEnterpriseName(entity.getEnterpriseName());
        record.setSecurityAccountName(entity.getSecurityAccountName());
        return record;
    }
}
