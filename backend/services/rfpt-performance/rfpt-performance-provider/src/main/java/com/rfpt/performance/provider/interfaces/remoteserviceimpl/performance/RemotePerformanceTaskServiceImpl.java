package com.rfpt.performance.provider.interfaces.remoteserviceimpl.performance;

import com.rfpt.performance.api.dto.performance.PerformanceTaskDto;
import com.rfpt.performance.api.param.performance.PerformanceTaskCreateParam;
import com.rfpt.performance.api.query.performance.PerformanceTaskPageParam;
import com.rfpt.performance.api.remoteservice.performance.RemotePerformanceTaskService;
import com.rfpt.performance.provider.application.manager.performance.PerformanceTaskManager;
import com.rfpt.performance.provider.application.result.performance.PerformanceTaskResult;
import com.rfpt.performance.provider.interfaces.remoteserviceimpl.performance.converter.RemotePerformanceTaskConverter;
import org.apache.dubbo.config.annotation.DubboService;
import com.zy.common.core.bo.PageResp;

import javax.annotation.Resource;

/**
 * 绩效任务 RPC 服务实现。
 */
@DubboService
public class RemotePerformanceTaskServiceImpl implements RemotePerformanceTaskService {

    /**
     * 绩效任务应用编排。
     */
    @Resource
    private PerformanceTaskManager performanceTaskManager;

    /**
     * 创建绩效任务。
     *
     * @param param 绩效任务创建入参
     * @return 绩效任务信息
     */
    @Override
    public PerformanceTaskDto createTask(PerformanceTaskCreateParam param) {
        PerformanceTaskResult result = performanceTaskManager.createTask(RemotePerformanceTaskConverter.toCreateCommand(param));
        return RemotePerformanceTaskConverter.toDto(result);
    }

    /**
     * 分页查询绩效任务。
     *
     * @param param 绩效任务分页查询入参
     * @return 绩效任务分页
     */
    @Override
    public PageResp<PerformanceTaskDto> pageTasks(PerformanceTaskPageParam param) {
        return RemotePerformanceTaskConverter.toDtoPage(
                performanceTaskManager.pageTasks(RemotePerformanceTaskConverter.toPageQuery(param)));
    }
}
