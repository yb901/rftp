package com.rf.performance.api.remoteservice.performance;

import com.rf.performance.api.dto.performance.EmployeePerformanceImportResultDto;
import com.rf.performance.api.dto.performance.admin.EmployeePerformanceAdjustDto;
import com.rf.performance.api.dto.performance.admin.EmployeePerformanceRecordDto;
import com.rf.performance.api.param.performance.EmployeePerformanceImportParam;
import com.rf.performance.api.param.performance.admin.EmployeePerformanceAdjustParam;
import com.rf.performance.api.param.performance.admin.EmployeePerformanceFeedbackHandleParam;
import com.rf.performance.api.query.performance.EmployeePerformancePageParam;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.exception.BusinessException;

/**
 * 员工绩效记录 RPC 服务。
 */
public interface RemoteEmployeePerformanceService {

    /**
     * 导入员工绩效记录。
     *
     * @param param 员工绩效导入入参
     * @return 员工绩效导入结果
     */
    EmployeePerformanceImportResultDto importRecords(EmployeePerformanceImportParam param) throws BusinessException;

    /**
     * 分页查询员工绩效记录。
     *
     * @param param 分页查询入参
     * @return 员工绩效记录分页
     */
    PageResp<EmployeePerformanceRecordDto> pageRecords(EmployeePerformancePageParam param);

    /**
     * 删除员工绩效记录。
     *
     * @param recordId 员工绩效记录 ID
     * @throws BusinessException 业务异常
     */
    void deleteRecord(Long recordId) throws BusinessException;

    /**
     * 调整员工绩效。
     *
     * @param param 员工绩效调整入参
     * @return 员工绩效调整结果
     */
    EmployeePerformanceAdjustDto adjustPerformance(EmployeePerformanceAdjustParam param) throws BusinessException;

    /**
     * 处理反馈且不调整绩效。
     *
     * @param param 反馈处理入参
     */
    void handleFeedbackUnchanged(EmployeePerformanceFeedbackHandleParam param) throws BusinessException;
}
