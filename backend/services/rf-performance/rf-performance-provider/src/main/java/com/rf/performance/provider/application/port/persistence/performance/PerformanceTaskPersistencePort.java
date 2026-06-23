package com.rf.performance.provider.application.port.persistence.performance;

import com.rf.performance.provider.application.port.persistence.performance.data.PerformanceTaskData;
import com.rf.performance.provider.application.port.persistence.performance.record.PerformanceTaskRecord;
import com.rf.performance.provider.application.query.performance.PerformanceTaskPageQuery;

import java.util.List;

/**
 * 绩效任务持久化端口。
 */
public interface PerformanceTaskPersistencePort {

    /**
     * 按 ID 查询绩效任务。
     *
     * @param id 绩效任务 ID
     * @return 绩效任务读取记录
     */
    PerformanceTaskRecord getById(Long id);

    /**
     * 按 ID 批量查询绩效任务。
     *
     * @param ids 绩效任务 ID
     * @return 绩效任务读取记录
     */
    List<PerformanceTaskRecord> listByIds(List<Long> ids);

    /**
     * 按条件统计绩效任务。
     *
     * @param query 查询条件
     * @return 总数
     */
    long count(PerformanceTaskPageQuery query);

    /**
     * 按条件分页查询绩效任务。
     *
     * @param query 查询条件
     * @return 绩效任务
     */
    List<PerformanceTaskRecord> page(PerformanceTaskPageQuery query);

    /**
     * 查询首次确认超期任务 ID。
     *
     * @param limit 最大数量
     * @return 绩效任务 ID
     */
    List<Long> listExpiredFirstConfirmTaskIds(int limit);

    /**
     * 查询二次确认超期任务 ID。
     *
     * @param limit 最大数量
     * @return 绩效任务 ID
     */
    List<Long> listExpiredSecondConfirmTaskIds(int limit);

    /**
     * 新增绩效任务。
     *
     * @param data 绩效任务写入数据
     * @return 绩效任务读取记录
     */
    PerformanceTaskRecord insert(PerformanceTaskData data);

    /**
     * 更新绩效任务状态。
     *
     * @param id 绩效任务 ID
     * @param status 任务状态编码
     * @return 是否更新成功
     */
    boolean updateStatus(Long id, String status);

    /**
     * 增加绩效任务员工数量。
     *
     * @param id 绩效任务 ID
     * @param count 增加数量
     * @return 是否更新成功
     */
    boolean increaseTotalCount(Long id, int count);

    /**
     * 删除绩效任务。
     *
     * @param id 绩效任务 ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
}
