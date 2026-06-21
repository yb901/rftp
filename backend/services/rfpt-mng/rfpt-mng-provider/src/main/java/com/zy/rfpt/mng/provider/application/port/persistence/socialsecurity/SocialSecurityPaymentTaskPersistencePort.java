package com.zy.rfpt.mng.provider.application.port.persistence.socialsecurity;

import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;

import java.util.List;

/**
 * 社保缴费任务持久化端口。
 */
public interface SocialSecurityPaymentTaskPersistencePort {

    /** 批量新增任务。 */
    int batchInsert(List<SocialSecurityPaymentTaskEntity> entities);

    /** 按编号查询任务。 */
    SocialSecurityPaymentTaskEntity findById(Long id);

    /** 更新任务为待重试。 */
    int markRetry(Long id, String operator);

    /** 按条件统计任务数。 */
    long count(SocialSecurityPaymentTaskQuery query);

    /** 按条件分页查询任务。 */
    List<SocialSecurityPaymentTaskEntity> page(SocialSecurityPaymentTaskQuery query);

    /** 按税号批量查询企业信息。 */
    List<SocialSecurityPaymentTaskEntity> listEnterpriseInfoByTaxNos(List<String> taxNos);
}
