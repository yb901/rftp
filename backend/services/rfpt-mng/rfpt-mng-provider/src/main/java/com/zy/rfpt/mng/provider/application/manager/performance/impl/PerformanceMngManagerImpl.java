package com.zy.rfpt.mng.provider.application.manager.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rfpt.performance.api.dto.performance.EmployeePerformanceImportResultDto;
import com.rfpt.performance.api.dto.performance.admin.EmployeePerformanceAdjustDto;
import com.rfpt.performance.api.dto.performance.admin.EmployeePerformanceRecordDto;
import com.rfpt.performance.api.dto.performance.item.EmployeePerformanceImportErrorDto;
import com.rfpt.performance.api.dto.performance.PerformanceTaskDto;
import com.rfpt.performance.api.param.performance.EmployeePerformanceImportParam;
import com.rfpt.performance.api.param.performance.admin.EmployeePerformanceAdjustParam;
import com.rfpt.performance.api.param.performance.item.EmployeePerformanceImportItemParam;
import com.rfpt.performance.api.param.performance.PerformanceTaskCreateParam;
import com.rfpt.performance.api.query.performance.EmployeePerformancePageParam;
import com.rfpt.performance.api.query.performance.PerformanceTaskPageParam;
import com.rfpt.performance.api.remoteservice.performance.RemoteEmployeePerformanceService;
import com.rfpt.performance.api.remoteservice.performance.RemotePerformanceTaskService;
import com.zy.common.core.bo.PageResp;
import com.zy.rfpt.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.zy.rfpt.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.zy.rfpt.mng.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import com.zy.rfpt.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.zy.rfpt.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.zy.rfpt.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.zy.rfpt.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.zy.rfpt.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.zy.rfpt.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.zy.rfpt.mng.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import com.zy.rfpt.mng.provider.application.result.performance.PerformanceTaskResult;
import com.zy.rfpt.mng.provider.application.manager.performance.PerformanceMngManager;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端员工绩效应用编排实现。
 */
@Service
public class PerformanceMngManagerImpl implements PerformanceMngManager {

    /**
     * 绩效任务 RPC 服务。
     */
    @DubboReference(check = false)
    private RemotePerformanceTaskService remotePerformanceTaskService;

    /**
     * 员工绩效记录 RPC 服务。
     */
    @DubboReference(check = false)
    private RemoteEmployeePerformanceService remoteEmployeePerformanceService;

    /**
     * 创建绩效任务。
     *
     * @param command 绩效任务创建命令
     * @return 绩效任务信息
     */
    @Override
    public PerformanceTaskResult createTask(PerformanceTaskCreateCommand command) {
        PerformanceTaskCreateParam param = BeanUtil.copyProperties(command, PerformanceTaskCreateParam.class);
        PerformanceTaskDto dto = remotePerformanceTaskService.createTask(param);
        return BeanUtil.copyProperties(dto, PerformanceTaskResult.class);
    }

    /**
     * 分页查询绩效任务。
     *
     * @param query 绩效任务分页查询条件
     * @return 绩效任务分页
     */
    @Override
    public PageResp<PerformanceTaskResult> pageTasks(PerformanceTaskPageQuery query) {
        PerformanceTaskPageParam param = BeanUtil.copyProperties(query, PerformanceTaskPageParam.class);
        PageResp<PerformanceTaskDto> dtoPage = remotePerformanceTaskService.pageTasks(param);
        return toTaskResultPage(dtoPage);
    }

    /**
     * 导入员工绩效记录。
     *
     * @param command 员工绩效导入命令
     * @return 员工绩效导入结果
     */
    @Override
    public EmployeePerformanceImportResult importRecords(EmployeePerformanceImportCommand command) {
        EmployeePerformanceImportCommand safeCommand = command == null ? new EmployeePerformanceImportCommand() : command;
        EmployeePerformanceImportParam param = BeanUtil.copyProperties(safeCommand, EmployeePerformanceImportParam.class);
        param.setRecords(toImportItemParams(safeCommand.getRecords()));
        EmployeePerformanceImportResultDto dto = remoteEmployeePerformanceService.importRecords(param);
        EmployeePerformanceImportResult result = BeanUtil.copyProperties(dto, EmployeePerformanceImportResult.class);
        result.setErrors(toImportErrorResults(dto == null ? null : dto.getErrors()));
        return result;
    }

    /**
     * 分页查询员工绩效记录。
     *
     * @param query 员工绩效分页查询条件
     * @return 员工绩效分页结果
     */
    @Override
    public PageResp<EmployeePerformanceRecordResult> pageRecords(EmployeePerformancePageQuery query) {
        EmployeePerformancePageParam param = BeanUtil.copyProperties(query, EmployeePerformancePageParam.class);
        PageResp<EmployeePerformanceRecordDto> dtoPage = remoteEmployeePerformanceService.pageRecords(param);
        return toRecordResultPage(dtoPage);
    }

    /**
     * 调整员工绩效。
     *
     * @param command 员工绩效调整命令
     * @return 员工绩效调整结果
     */
    @Override
    public EmployeePerformanceAdjustResult adjustPerformance(EmployeePerformanceAdjustCommand command) {
        EmployeePerformanceAdjustParam param = BeanUtil.copyProperties(command, EmployeePerformanceAdjustParam.class);
        EmployeePerformanceAdjustDto dto = remoteEmployeePerformanceService.adjustPerformance(param);
        return BeanUtil.copyProperties(dto, EmployeePerformanceAdjustResult.class);
    }

    /**
     * 转换员工绩效导入明细 RPC 入参。
     *
     * @param records 员工绩效导入明细命令
     * @return 员工绩效导入明细 RPC 入参
     */
    private List<EmployeePerformanceImportItemParam> toImportItemParams(List<EmployeePerformanceImportItemCommand> records) {
        if (records == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(records, EmployeePerformanceImportItemParam.class);
    }

    /**
     * 转换员工绩效导入错误明细。
     *
     * @param errors RPC 错误明细
     * @return 应用层错误明细
     */
    private List<EmployeePerformanceImportErrorResult> toImportErrorResults(List<EmployeePerformanceImportErrorDto> errors) {
        if (errors == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(errors, EmployeePerformanceImportErrorResult.class);
    }

    /**
     * 转换员工绩效分页结果。
     *
     * @param dtoPage RPC 分页结果
     * @return 应用层分页结果
     */
    private PageResp<EmployeePerformanceRecordResult> toRecordResultPage(PageResp<EmployeePerformanceRecordDto> dtoPage) {
        if (dtoPage == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<EmployeePerformanceRecordResult> resultPage = new PageResp<>();
        resultPage.setPagination(dtoPage.getPagination());
        resultPage.setList(BeanUtil.copyToList(dtoPage.getList(), EmployeePerformanceRecordResult.class));
        return resultPage;
    }

    /**
     * 转换绩效任务分页结果。
     *
     * @param dtoPage RPC 分页结果
     * @return 应用层分页结果
     */
    private PageResp<PerformanceTaskResult> toTaskResultPage(PageResp<PerformanceTaskDto> dtoPage) {
        if (dtoPage == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<PerformanceTaskResult> resultPage = new PageResp<>();
        resultPage.setPagination(dtoPage.getPagination());
        resultPage.setList(BeanUtil.copyToList(dtoPage.getList(), PerformanceTaskResult.class));
        return resultPage;
    }
}
