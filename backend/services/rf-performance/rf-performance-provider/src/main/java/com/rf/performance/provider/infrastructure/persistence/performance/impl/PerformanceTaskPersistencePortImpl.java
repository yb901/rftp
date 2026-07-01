package com.rf.performance.provider.infrastructure.persistence.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.port.persistence.performance.PerformanceTaskPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.data.PerformanceTaskData;
import com.rf.performance.provider.application.port.persistence.performance.record.PerformanceTaskRecord;
import com.rf.performance.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.PerformanceTaskEntity;
import com.rf.performance.provider.infrastructure.persistence.performance.mapper.PerformanceTaskMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 绩效任务持久化端口实现。
 */
@Repository
public class PerformanceTaskPersistencePortImpl implements PerformanceTaskPersistencePort {

    /**
     * 绩效任务 Mapper。
     */
    @Resource
    private PerformanceTaskMapper performanceTaskMapper;

    /**
     * 按 ID 查询绩效任务。
     *
     * @param id 绩效任务 ID
     * @return 绩效任务读取记录
     */
    @Override
    public PerformanceTaskRecord getById(Long id) {
        PerformanceTaskEntity entity = performanceTaskMapper.getById(id);
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, PerformanceTaskRecord.class);
    }

    /**
     * 按 ID 批量查询绩效任务。
     *
     * @param ids 绩效任务 ID
     * @return 绩效任务读取记录
     */
    @Override
    public List<PerformanceTaskRecord> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<PerformanceTaskEntity> entities = performanceTaskMapper.listByIds(ids);
        return BeanUtil.copyToList(entities, PerformanceTaskRecord.class);
    }

    /**
     * 判断绩效任务描述是否已存在。
     *
     * @param performanceDescription 绩效描述
     * @return 是否已存在
     */
    @Override
    public boolean existsByPerformanceDescription(String performanceDescription) {
        return performanceTaskMapper.countByPerformanceDescription(performanceDescription) > 0;
    }

    /**
     * 按条件统计绩效任务。
     *
     * @param query 查询条件
     * @return 总数
     */
    @Override
    public long count(PerformanceTaskPageQuery query) {
        return performanceTaskMapper.count(query);
    }

    /**
     * 按条件分页查询绩效任务。
     *
     * @param query 查询条件
     * @return 绩效任务
     */
    @Override
    public List<PerformanceTaskRecord> page(PerformanceTaskPageQuery query) {
        int offset = Math.max(query.getPage() - 1, 0) * query.getSize();
        List<PerformanceTaskEntity> entities = performanceTaskMapper.page(query, offset, query.getSize());
        return BeanUtil.copyToList(entities, PerformanceTaskRecord.class);
    }

    /**
     * 查询首次确认超期任务 ID。
     *
     * @param limit 最大数量
     * @return 绩效任务 ID
     */
    @Override
    public List<Long> listExpiredFirstConfirmTaskIds(int limit) {
        return performanceTaskMapper.listExpiredFirstConfirmTaskIds(limit);
    }

    /**
     * 查询二次确认超期任务 ID。
     *
     * @param limit 最大数量
     * @return 绩效任务 ID
     */
    @Override
    public List<Long> listExpiredSecondConfirmTaskIds(int limit) {
        return performanceTaskMapper.listExpiredSecondConfirmTaskIds(limit);
    }

    /**
     * 新增绩效任务。
     *
     * @param data 绩效任务写入数据
     * @return 绩效任务读取记录
     */
    @Override
    public PerformanceTaskRecord insert(PerformanceTaskData data) {
        PerformanceTaskEntity entity = BeanUtil.copyProperties(data, PerformanceTaskEntity.class);
        entity.setTotalCount(0);
        entity.setConfirmedCount(0);
        entity.setFeedbackCount(0);
        entity.setAutoConfirmedCount(0);
        entity.setIsDeleted(0);
        performanceTaskMapper.insert(entity);
        return BeanUtil.copyProperties(entity, PerformanceTaskRecord.class);
    }

    /**
     * 更新绩效任务状态。
     *
     * @param id 绩效任务 ID
     * @param status 任务状态编码
     * @return 是否更新成功
     */
    @Override
    public boolean updateStatus(Long id, String status) {
        return performanceTaskMapper.updateStatus(id, status) > 0;
    }

    /**
     * 增加绩效任务员工数量。
     *
     * @param id 绩效任务 ID
     * @param count 增加数量
     * @return 是否更新成功
     */
    @Override
    public boolean increaseTotalCount(Long id, int count) {
        return performanceTaskMapper.increaseTotalCount(id, count) > 0;
    }

    /**
     * 增加绩效任务确认数量。
     *
     * @param id 绩效任务 ID
     * @param count 增加数量
     * @return 是否更新成功
     */
    @Override
    public boolean increaseConfirmedCount(Long id, int count) {
        return performanceTaskMapper.increaseConfirmedCount(id, count) > 0;
    }

    /**
     * 增加绩效任务反馈数量。
     *
     * @param id 绩效任务 ID
     * @param count 增加数量
     * @return 是否更新成功
     */
    @Override
    public boolean increaseFeedbackCount(Long id, int count) {
        return performanceTaskMapper.increaseFeedbackCount(id, count) > 0;
    }

    /**
     * 增加绩效任务自动确认数量。
     *
     * @param id 绩效任务 ID
     * @param count 增加数量
     * @return 是否更新成功
     */
    @Override
    public boolean increaseAutoConfirmedCount(Long id, int count) {
        return performanceTaskMapper.increaseAutoConfirmedCount(id, count) > 0;
    }

    /**
     * 扣减绩效任务统计。
     *
     * @param id 绩效任务 ID
     * @param totalCount 总数扣减数量
     * @param confirmedCount 确认数扣减数量
     * @param feedbackCount 反馈数扣减数量
     * @param autoConfirmedCount 自动确认数扣减数量
     * @return 是否更新成功
     */
    @Override
    public boolean decreaseStats(Long id, int totalCount, int confirmedCount, int feedbackCount, int autoConfirmedCount) {
        return performanceTaskMapper.decreaseStats(id, totalCount, confirmedCount, feedbackCount, autoConfirmedCount) > 0;
    }

    /**
     * 删除绩效任务。
     *
     * @param id 绩效任务 ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        return performanceTaskMapper.deleteById(id) > 0;
    }
}
