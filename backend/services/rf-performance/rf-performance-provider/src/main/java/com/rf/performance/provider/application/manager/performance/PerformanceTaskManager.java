package com.rf.performance.provider.application.manager.performance;

import com.rf.performance.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.performance.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.performance.provider.application.result.performance.PerformanceTaskResult;
import com.zy.common.core.bo.PageResp;

/**
 * 绩效任务应用编排。
 */
public interface PerformanceTaskManager {

    /**
     * 创建绩效任务。
     *
     * @param command 绩效任务创建命令
     * @return 绩效任务信息
     */
    PerformanceTaskResult createTask(PerformanceTaskCreateCommand command);

    /**
     * 分页查询绩效任务。
     *
     * @param query 绩效任务分页查询条件
     * @return 绩效任务分页
     */
    PageResp<PerformanceTaskResult> pageTasks(PerformanceTaskPageQuery query);
}
