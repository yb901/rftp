package com.rf.mng.provider.application.port.gateway.performance;

import com.rf.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.mng.provider.application.result.performance.PerformanceTaskResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.zy.common.core.bo.PageResp;

/**
 * 员工绩效内部服务网关。
 */
public interface EmployeePerformanceGateway {

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
     * @return 绩效任务分页结果
     */
    PageResp<PerformanceTaskResult> pageTasks(PerformanceTaskPageQuery query);

    /**
     * 导入员工绩效。
     *
     * @param command 员工绩效导入命令
     * @return 导入结果
     */
    EmployeePerformanceImportResult importRecords(EmployeePerformanceImportCommand command);

    /**
     * 分页查询员工绩效。
     *
     * @param query 员工绩效分页查询条件
     * @return 员工绩效分页结果
     */
    PageResp<EmployeePerformanceRecordResult> pageRecords(EmployeePerformancePageQuery query);

    /**
     * 调整员工绩效。
     *
     * @param command 员工绩效调整命令
     * @return 调整结果
     */
    EmployeePerformanceAdjustResult adjustPerformance(EmployeePerformanceAdjustCommand command);
}
