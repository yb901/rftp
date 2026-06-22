package com.rf.mng.provider.application.manager.performance.impl;

import com.rf.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.mng.provider.application.manager.performance.PerformanceMngManager;
import com.rf.mng.provider.application.port.gateway.performance.EmployeePerformanceGateway;
import com.rf.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.mng.provider.application.result.performance.PerformanceTaskResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.zy.common.core.bo.PageResp;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 管理端员工绩效应用编排实现。
 */
@Service
public class PerformanceMngManagerImpl implements PerformanceMngManager {

    /** 员工绩效内部服务网关。 */
    @Resource
    private EmployeePerformanceGateway employeePerformanceGateway;

    /**
     * 创建绩效任务。
     *
     * @param command 绩效任务创建命令
     * @return 绩效任务信息
     */
    @Override
    public PerformanceTaskResult createTask(PerformanceTaskCreateCommand command) {
        return employeePerformanceGateway.createTask(command);
    }

    /**
     * 分页查询绩效任务。
     *
     * @param query 绩效任务分页查询条件
     * @return 绩效任务分页
     */
    @Override
    public PageResp<PerformanceTaskResult> pageTasks(PerformanceTaskPageQuery query) {
        return employeePerformanceGateway.pageTasks(query);
    }

    /**
     * 导入员工绩效记录。
     *
     * @param command 员工绩效导入命令
     * @return 员工绩效导入结果
     */
    @Override
    public EmployeePerformanceImportResult importRecords(EmployeePerformanceImportCommand command) {
        return employeePerformanceGateway.importRecords(command);
    }

    /**
     * 分页查询员工绩效记录。
     *
     * @param query 员工绩效分页查询条件
     * @return 员工绩效分页结果
     */
    @Override
    public PageResp<EmployeePerformanceRecordResult> pageRecords(EmployeePerformancePageQuery query) {
        return employeePerformanceGateway.pageRecords(query);
    }

    /**
     * 调整员工绩效。
     *
     * @param command 员工绩效调整命令
     * @return 员工绩效调整结果
     */
    @Override
    public EmployeePerformanceAdjustResult adjustPerformance(EmployeePerformanceAdjustCommand command) {
        return employeePerformanceGateway.adjustPerformance(command);
    }
}
