package com.rf.mng.provider.infrastructure.persistence.robot.socialsecurity.mapper;

import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;
import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社保缴费机器人基础信息 Mapper。
 */
@Mapper
public interface SocialSecurityRobotReferenceMapper {

    /** 按地区编码批量查询地区名称。 */
    List<SocialSecurityPaymentBatchEntity> listRegionNamesByRegionCodes(@Param("regionCodes") List<String> regionCodes);

    /** 按税号批量查询企业信息。 */
    List<SocialSecurityPaymentTaskEntity> listEnterpriseInfoByTaxNos(@Param("taxNos") List<String> taxNos);
}
