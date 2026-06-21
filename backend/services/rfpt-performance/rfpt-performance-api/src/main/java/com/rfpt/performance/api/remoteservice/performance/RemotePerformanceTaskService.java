package com.rfpt.performance.api.remoteservice.performance;

import com.rfpt.performance.api.dto.performance.PerformanceTaskDto;
import com.rfpt.performance.api.param.performance.PerformanceTaskCreateParam;
import com.rfpt.performance.api.query.performance.PerformanceTaskPageParam;
import com.zy.common.core.bo.PageResp;

/**
 * 绩效任务 RPC 服务。
 */
public interface RemotePerformanceTaskService {

    /**
     * 创建绩效任务。
     *
     * @param param 绩效任务创建入参
     * @return 绩效任务信息
     */
    PerformanceTaskDto createTask(PerformanceTaskCreateParam param);

    /**
     * 分页查询绩效任务。
     *
     * @param param 绩效任务分页查询入参
     * @return 绩效任务分页
     */
    PageResp<PerformanceTaskDto> pageTasks(PerformanceTaskPageParam param);
}
