package com.rf.mng.provider.application.port.persistence.socialsecurity;

import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentBatchRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentTaskRecord;

import java.util.List;

/**
 * 社保缴费机器人基础信息持久化端口。
 */
public interface SocialSecurityRobotReferencePersistencePort {

    /** 按地区编码批量查询地区名称。 */
    List<SocialSecurityPaymentBatchRecord> listRegionNamesByRegionCodes(List<String> regionCodes);

    /** 按税号批量查询企业信息。 */
    List<SocialSecurityPaymentTaskRecord> listEnterpriseInfoByTaxNos(List<String> taxNos);
}
