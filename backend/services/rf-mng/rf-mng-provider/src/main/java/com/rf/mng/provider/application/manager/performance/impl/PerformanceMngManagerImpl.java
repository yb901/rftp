package com.rf.mng.provider.application.manager.performance.impl;

import com.rf.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceFeedbackHandleCommand;
import com.rf.mng.provider.application.manager.performance.PerformanceMngManager;
import com.rf.mng.provider.application.port.gateway.performance.EmployeePerformanceGateway;
import com.rf.mng.provider.application.port.persistence.performance.EmployeePerformanceImportUploadPersistencePort;
import com.rf.mng.provider.application.port.persistence.performance.data.EmployeePerformanceImportUploadData;
import com.rf.mng.provider.application.port.persistence.performance.record.EmployeePerformanceImportUploadRecord;
import com.rf.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportUploadResult;
import com.rf.mng.provider.application.result.performance.PerformanceTaskResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.zy.common.core.bo.PageResp;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理端员工绩效应用编排实现。
 */
@Service
public class PerformanceMngManagerImpl implements PerformanceMngManager {

    /** 员工绩效内部服务网关。 */
    @Resource
    private EmployeePerformanceGateway employeePerformanceGateway;

    /** 员工绩效导入上传记录持久化端口。 */
    @Resource
    private EmployeePerformanceImportUploadPersistencePort importUploadPersistencePort;

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
     * 启用绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    public void enableTask(Long taskId) {
        employeePerformanceGateway.enableTask(taskId);
    }

    /**
     * 停用绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    public void disableTask(Long taskId) {
        employeePerformanceGateway.disableTask(taskId);
    }

    /**
     * 删除绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    public void deleteTask(Long taskId) {
        employeePerformanceGateway.deleteTask(taskId);
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
     * 保存员工绩效导入上传记录。
     *
     * @param result 上传记录
     * @return 上传记录结果
     */
    @Override
    public EmployeePerformanceImportUploadResult saveImportUpload(EmployeePerformanceImportUploadResult result) {
        EmployeePerformanceImportUploadData data = BeanCopy.toData(result);
        importUploadPersistencePort.insert(data);
        result.setId(data.getId());
        return result;
    }

    /**
     * 查询员工绩效导入上传记录。
     *
     * @param taskId 绩效任务ID，可为空
     * @param limit 查询数量
     * @return 上传记录列表
     */
    @Override
    public List<EmployeePerformanceImportUploadResult> listImportUploads(Long taskId, int limit) {
        return importUploadPersistencePort.listRecent(taskId, limit).stream().map(BeanCopy::toResult).toList();
    }

    /**
     * 获取员工绩效导入上传记录。
     *
     * @param uploadId 上传记录ID
     * @return 上传记录
     */
    @Override
    public EmployeePerformanceImportUploadResult getImportUpload(Long uploadId) {
        EmployeePerformanceImportUploadRecord record = importUploadPersistencePort.getById(uploadId);
        if (record == null) {
            return null;
        }
        return BeanCopy.toResult(record);
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

    /**
     * 处理反馈且不调整绩效。
     *
     * @param command 员工绩效反馈处理命令
     */
    @Override
    public void handleFeedbackUnchanged(EmployeePerformanceFeedbackHandleCommand command) {
        employeePerformanceGateway.handleFeedbackUnchanged(command);
    }

    /**
     * 员工绩效导入上传记录对象转换。
     */
    private static class BeanCopy {

        /**
         * 转换写入数据。
         *
         * @param result 上传记录结果
         * @return 上传记录写入数据
         */
        private static EmployeePerformanceImportUploadData toData(EmployeePerformanceImportUploadResult result) {
            EmployeePerformanceImportUploadData data = new EmployeePerformanceImportUploadData();
            data.setId(result.getId());
            data.setTaskId(result.getTaskId());
            data.setTaskName(result.getTaskName());
            data.setFileName(result.getFileName());
            data.setOriginalContentType(result.getOriginalContentType());
            data.setOriginalFileUrl(result.getOriginalFileUrl());
            data.setFailureFileName(result.getFailureFileName());
            data.setFailureFileUrl(result.getFailureFileUrl());
            data.setTotalCount(result.getTotalCount());
            data.setSuccessCount(result.getSuccessCount());
            data.setFailCount(result.getFailCount());
            data.setStatus(result.getStatus());
            data.setErrorMessage(result.getErrorMessage());
            data.setCreateAdminId(result.getCreateAdminId());
            data.setCreateAdminName(result.getCreateAdminName());
            return data;
        }

        /**
         * 转换应用结果。
         *
         * @param record 上传记录读取结果
         * @return 上传记录应用结果
         */
        private static EmployeePerformanceImportUploadResult toResult(EmployeePerformanceImportUploadRecord record) {
            EmployeePerformanceImportUploadResult result = new EmployeePerformanceImportUploadResult();
            result.setId(record.getId());
            result.setTaskId(record.getTaskId());
            result.setTaskName(record.getTaskName());
            result.setFileName(record.getFileName());
            result.setOriginalContentType(record.getOriginalContentType());
            result.setOriginalFileUrl(record.getOriginalFileUrl());
            result.setFailureFileName(record.getFailureFileName());
            result.setFailureFileUrl(record.getFailureFileUrl());
            result.setTotalCount(record.getTotalCount());
            result.setSuccessCount(record.getSuccessCount());
            result.setFailCount(record.getFailCount());
            result.setStatus(record.getStatus());
            result.setErrorMessage(record.getErrorMessage());
            result.setCreateAdminId(record.getCreateAdminId());
            result.setCreateAdminName(record.getCreateAdminName());
            result.setGmtCreate(record.getGmtCreate());
            return result;
        }
    }
}
