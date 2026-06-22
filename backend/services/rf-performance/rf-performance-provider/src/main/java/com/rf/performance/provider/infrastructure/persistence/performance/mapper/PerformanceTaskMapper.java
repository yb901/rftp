package com.rf.performance.provider.infrastructure.persistence.performance.mapper;

import com.rf.performance.provider.infrastructure.persistence.performance.entity.PerformanceTaskEntity;
import com.rf.performance.provider.application.query.performance.PerformanceTaskPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工绩效任务 Mapper。
 */
@Mapper
public interface PerformanceTaskMapper {

    /**
     * 按 ID 查询绩效任务。
     *
     * @param id 绩效任务 ID
     * @return 员工绩效任务实体
     */
    PerformanceTaskEntity getById(Long id);

    /**
     * 按 ID 批量查询绩效任务。
     *
     * @param ids 绩效任务 ID
     * @return 员工绩效任务实体
     */
    List<PerformanceTaskEntity> listByIds(@Param("ids") List<Long> ids);

    /**
     * 按条件统计绩效任务。
     *
     * @param query 查询条件
     * @return 总数
     */
    long count(@Param("query") PerformanceTaskPageQuery query);

    /**
     * 按条件分页查询绩效任务。
     *
     * @param query 查询条件
     * @param offset 偏移量
     * @param limit 数量
     * @return 绩效任务
     */
    List<PerformanceTaskEntity> page(@Param("query") PerformanceTaskPageQuery query,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 查询首次确认超期任务 ID。
     *
     * @param limit 最大数量
     * @return 绩效任务 ID
     */
    List<Long> listExpiredFirstConfirmTaskIds(@Param("limit") int limit);

    /**
     * 查询二次确认超期任务 ID。
     *
     * @param limit 最大数量
     * @return 绩效任务 ID
     */
    List<Long> listExpiredSecondConfirmTaskIds(@Param("limit") int limit);

    /**
     * 新增绩效任务。
     *
     * @param entity 员工绩效任务实体
     * @return 影响行数
     */
    int insert(PerformanceTaskEntity entity);

    /**
     * 增加绩效任务员工数量。
     *
     * @param id 绩效任务 ID
     * @param count 增加数量
     * @return 影响行数
     */
    int increaseTotalCount(@Param("id") Long id, @Param("count") int count);
}
