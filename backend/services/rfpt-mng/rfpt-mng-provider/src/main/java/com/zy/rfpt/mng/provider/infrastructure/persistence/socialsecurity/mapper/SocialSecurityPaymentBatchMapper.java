package com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.mapper;

import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社保缴费批次 Mapper。
 */
@Mapper
public interface SocialSecurityPaymentBatchMapper {

    /** 新增批次。 */
    int insert(SocialSecurityPaymentBatchEntity entity);

    /** 按条件统计。 */
    long count(@Param("query") SocialSecurityPaymentBatchQuery query);

    /** 按条件分页。 */
    List<SocialSecurityPaymentBatchEntity> page(@Param("query") SocialSecurityPaymentBatchQuery query,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);

    /** 按地区编码批量查询地区名称。 */
    List<SocialSecurityPaymentBatchEntity> listRegionNamesByRegionCodes(@Param("regionCodes") List<String> regionCodes);
}
