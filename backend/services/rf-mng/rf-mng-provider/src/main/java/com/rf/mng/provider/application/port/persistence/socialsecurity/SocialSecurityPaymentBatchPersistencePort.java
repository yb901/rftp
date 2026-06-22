package com.rf.mng.provider.application.port.persistence.socialsecurity;

import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentBatchData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentBatchRecord;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;

import java.util.List;

/**
 * 社保缴费批次持久化端口。
 */
public interface SocialSecurityPaymentBatchPersistencePort {

    /** 新增批次。 */
    int insert(SocialSecurityPaymentBatchData data);

    /** 按条件统计批次数。 */
    long count(SocialSecurityPaymentBatchQuery query);

    /** 按条件分页查询批次。 */
    List<SocialSecurityPaymentBatchRecord> page(SocialSecurityPaymentBatchQuery query);

}
