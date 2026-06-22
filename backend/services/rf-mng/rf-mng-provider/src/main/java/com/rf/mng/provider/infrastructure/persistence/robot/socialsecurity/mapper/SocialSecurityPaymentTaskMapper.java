package com.rf.mng.provider.infrastructure.persistence.robot.socialsecurity.mapper;

import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.rf.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社保缴费任务 Mapper。
 */
@Mapper
public interface SocialSecurityPaymentTaskMapper {

    /** 批量新增任务。 */
    int batchInsert(@Param("list") List<SocialSecurityPaymentTaskEntity> list);

    /** 按编号查询。 */
    SocialSecurityPaymentTaskEntity findById(@Param("id") Long id);

    /** 标记待重试。 */
    int markRetry(@Param("id") Long id);

    /** 按条件统计。 */
    long count(@Param("query") SocialSecurityPaymentTaskQuery query);

    /** 按条件分页。 */
    List<SocialSecurityPaymentTaskEntity> page(@Param("query") SocialSecurityPaymentTaskQuery query,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

}
