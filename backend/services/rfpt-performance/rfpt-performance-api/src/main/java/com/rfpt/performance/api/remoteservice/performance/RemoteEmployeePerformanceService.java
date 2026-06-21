package com.rfpt.performance.api.remoteservice.performance;

import com.rfpt.performance.api.dto.performance.EmployeePerformanceImportResultDto;
import com.rfpt.performance.api.dto.performance.admin.EmployeePerformanceAdjustDto;
import com.rfpt.performance.api.dto.performance.admin.EmployeePerformanceRecordDto;
import com.rfpt.performance.api.param.performance.EmployeePerformanceImportParam;
import com.rfpt.performance.api.param.performance.admin.EmployeePerformanceAdjustParam;
import com.rfpt.performance.api.query.performance.EmployeePerformancePageParam;
import com.zy.common.core.bo.PageResp;

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
    EmployeePerformanceImportResultDto importRecords(EmployeePerformanceImportParam param);

    /**
     * 分页查询员工绩效记录。
     *
     * @param param 分页查询入参
     * @return 员工绩效记录分页
     */
    PageResp<EmployeePerformanceRecordDto> pageRecords(EmployeePerformancePageParam param);

    /**
     * 调整员工绩效。
     *
     * @param param 员工绩效调整入参
     * @return 员工绩效调整结果
     */
    EmployeePerformanceAdjustDto adjustPerformance(EmployeePerformanceAdjustParam param);
}
