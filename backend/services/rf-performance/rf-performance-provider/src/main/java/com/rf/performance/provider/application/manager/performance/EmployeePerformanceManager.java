package com.rf.performance.provider.application.manager.performance;

import com.rf.performance.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.performance.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.performance.provider.application.command.performance.admin.EmployeePerformanceFeedbackHandleCommand;
import com.rf.performance.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.performance.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.performance.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.performance.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.zy.common.core.bo.PageResp;

/**
 * 员工绩效记录应用编排。
 */
public interface EmployeePerformanceManager {

    /**
     * 导入员工绩效记录。
     *
     * @param command 员工绩效导入命令
     * @return 员工绩效导入结果
     */
    EmployeePerformanceImportResult importRecords(EmployeePerformanceImportCommand command);

    /**
     * 分页查询员工绩效记录。
     *
     * @param query 查询条件
     * @return 员工绩效记录分页
     */
    PageResp<EmployeePerformanceRecordResult> pageRecords(EmployeePerformancePageQuery query);

    /**
     * 调整员工绩效。
     *
     * @param command 员工绩效调整命令
     * @return 员工绩效调整结果
     */
    EmployeePerformanceAdjustResult adjustPerformance(EmployeePerformanceAdjustCommand command);

    /**
     * 处理反馈且不调整绩效。
     *
     * @param command 员工绩效反馈处理命令
     */
    void handleFeedbackUnchanged(EmployeePerformanceFeedbackHandleCommand command);
}
