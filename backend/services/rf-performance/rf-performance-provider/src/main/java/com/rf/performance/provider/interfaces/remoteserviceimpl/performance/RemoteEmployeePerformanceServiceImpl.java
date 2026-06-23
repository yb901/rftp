package com.rf.performance.provider.interfaces.remoteserviceimpl.performance;

import com.rf.performance.api.dto.performance.EmployeePerformanceImportResultDto;
import com.rf.performance.api.dto.performance.admin.EmployeePerformanceAdjustDto;
import com.rf.performance.api.dto.performance.admin.EmployeePerformanceRecordDto;
import com.rf.performance.api.param.performance.EmployeePerformanceImportParam;
import com.rf.performance.api.param.performance.admin.EmployeePerformanceAdjustParam;
import com.rf.performance.api.param.performance.admin.EmployeePerformanceFeedbackHandleParam;
import com.rf.performance.api.query.performance.EmployeePerformancePageParam;
import com.rf.performance.api.remoteservice.performance.RemoteEmployeePerformanceService;
import com.rf.performance.provider.application.manager.performance.EmployeePerformanceManager;
import com.rf.performance.provider.application.result.performance.EmployeePerformanceImportResult;
import com.zy.common.core.bo.PageResp;
import com.rf.performance.provider.interfaces.remoteserviceimpl.performance.converter.RemoteEmployeePerformanceConverter;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 员工绩效记录 RPC 服务实现。
 */
@DubboService
public class RemoteEmployeePerformanceServiceImpl implements RemoteEmployeePerformanceService {

    /**
     * 员工绩效记录应用编排。
     */
    @Resource
    private EmployeePerformanceManager employeePerformanceManager;

    /**
     * 导入员工绩效记录。
     *
     * @param param 员工绩效导入入参
     * @return 员工绩效导入结果
     */
    @Override
    public EmployeePerformanceImportResultDto importRecords(EmployeePerformanceImportParam param) {
        EmployeePerformanceImportResult result = employeePerformanceManager.importRecords(RemoteEmployeePerformanceConverter.toImportCommand(param));
        return RemoteEmployeePerformanceConverter.toImportDto(result);
    }

    /**
     * 分页查询员工绩效记录。
     *
     * @param param 分页查询入参
     * @return 员工绩效记录分页
     */
    @Override
    public PageResp<EmployeePerformanceRecordDto> pageRecords(EmployeePerformancePageParam param) {
        return RemoteEmployeePerformanceConverter.toRecordDtoPage(
                employeePerformanceManager.pageRecords(RemoteEmployeePerformanceConverter.toPageQuery(param)));
    }

    /**
     * 调整员工绩效。
     *
     * @param param 员工绩效调整入参
     * @return 员工绩效调整结果
     */
    @Override
    public EmployeePerformanceAdjustDto adjustPerformance(EmployeePerformanceAdjustParam param) {
        return RemoteEmployeePerformanceConverter.toAdjustDto(
                employeePerformanceManager.adjustPerformance(RemoteEmployeePerformanceConverter.toAdjustCommand(param)));
    }

    /**
     * 处理反馈且不调整绩效。
     *
     * @param param 反馈处理入参
     */
    @Override
    public void handleFeedbackUnchanged(EmployeePerformanceFeedbackHandleParam param) {
        employeePerformanceManager.handleFeedbackUnchanged(RemoteEmployeePerformanceConverter.toFeedbackHandleCommand(param));
    }
}
