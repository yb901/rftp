package com.zy.rfpt.mng.provider.application.port.persistence.socialsecurity;

import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;

import java.util.List;

/**
 * 社保缴费批次持久化端口。
 */
public interface SocialSecurityPaymentBatchPersistencePort {

    /** 新增批次。 */
    int insert(SocialSecurityPaymentBatchEntity entity);

    /** 按条件统计批次数。 */
    long count(SocialSecurityPaymentBatchQuery query);

    /** 按条件分页查询批次。 */
    List<SocialSecurityPaymentBatchEntity> page(SocialSecurityPaymentBatchQuery query);

    /** 按地区编码批量查询地区名称。 */
    List<SocialSecurityPaymentBatchEntity> listRegionNamesByRegionCodes(List<String> regionCodes);
}
