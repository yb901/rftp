package com.rf.mng.provider.infrastructure.port.gateway.internal.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rf.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.mng.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import com.rf.mng.provider.application.port.gateway.performance.EmployeePerformanceGateway;
import com.rf.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.mng.provider.application.result.performance.PerformanceTaskResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.rf.mng.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import com.rf.performance.api.dto.performance.EmployeePerformanceImportResultDto;
import com.rf.performance.api.dto.performance.PerformanceTaskDto;
import com.rf.performance.api.dto.performance.admin.EmployeePerformanceAdjustDto;
import com.rf.performance.api.dto.performance.admin.EmployeePerformanceRecordDto;
import com.rf.performance.api.dto.performance.item.EmployeePerformanceImportErrorDto;
import com.rf.performance.api.param.performance.EmployeePerformanceImportParam;
import com.rf.performance.api.param.performance.PerformanceTaskCreateParam;
import com.rf.performance.api.param.performance.admin.EmployeePerformanceAdjustParam;
import com.rf.performance.api.param.performance.item.EmployeePerformanceImportItemParam;
import com.rf.performance.api.query.performance.EmployeePerformancePageParam;
import com.rf.performance.api.query.performance.PerformanceTaskPageParam;
import com.rf.performance.api.remoteservice.performance.RemoteEmployeePerformanceService;
import com.rf.performance.api.remoteservice.performance.RemotePerformanceTaskService;
import com.zy.common.core.bo.PageResp;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工绩效内部 Dubbo 网关实现。
 */
@Component
public class EmployeePerformanceGatewayImpl implements EmployeePerformanceGateway {

    /** 绩效任务 RPC 服务。 */
    @DubboReference(check = false)
    private RemotePerformanceTaskService remotePerformanceTaskService;

    /** 员工绩效记录 RPC 服务。 */
    @DubboReference(check = false)
    private RemoteEmployeePerformanceService remoteEmployeePerformanceService;

    @Override
    public PerformanceTaskResult createTask(PerformanceTaskCreateCommand command) {
        PerformanceTaskCreateParam param = BeanUtil.copyProperties(command, PerformanceTaskCreateParam.class);
        PerformanceTaskDto dto = remotePerformanceTaskService.createTask(param);
        return BeanUtil.copyProperties(dto, PerformanceTaskResult.class);
    }

    @Override
    public PageResp<PerformanceTaskResult> pageTasks(PerformanceTaskPageQuery query) {
        PerformanceTaskPageParam param = BeanUtil.copyProperties(query, PerformanceTaskPageParam.class);
        PageResp<PerformanceTaskDto> dtoPage = remotePerformanceTaskService.pageTasks(param);
        return toTaskResultPage(dtoPage);
    }

    /**
     * 启用绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    public void enableTask(Long taskId) {
        remotePerformanceTaskService.enableTask(taskId);
    }

    /**
     * 停用绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    public void disableTask(Long taskId) {
        remotePerformanceTaskService.disableTask(taskId);
    }

    /**
     * 删除绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    public void deleteTask(Long taskId) {
        remotePerformanceTaskService.deleteTask(taskId);
    }

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

    @Override
    public PageResp<EmployeePerformanceRecordResult> pageRecords(EmployeePerformancePageQuery query) {
        EmployeePerformancePageParam param = BeanUtil.copyProperties(query, EmployeePerformancePageParam.class);
        PageResp<EmployeePerformanceRecordDto> dtoPage = remoteEmployeePerformanceService.pageRecords(param);
        return toRecordResultPage(dtoPage);
    }

    @Override
    public EmployeePerformanceAdjustResult adjustPerformance(EmployeePerformanceAdjustCommand command) {
        EmployeePerformanceAdjustParam param = BeanUtil.copyProperties(command, EmployeePerformanceAdjustParam.class);
        EmployeePerformanceAdjustDto dto = remoteEmployeePerformanceService.adjustPerformance(param);
        return BeanUtil.copyProperties(dto, EmployeePerformanceAdjustResult.class);
    }

    private List<EmployeePerformanceImportItemParam> toImportItemParams(List<EmployeePerformanceImportItemCommand> records) {
        if (records == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(records, EmployeePerformanceImportItemParam.class);
    }

    private List<EmployeePerformanceImportErrorResult> toImportErrorResults(List<EmployeePerformanceImportErrorDto> errors) {
        if (errors == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(errors, EmployeePerformanceImportErrorResult.class);
    }

    private PageResp<EmployeePerformanceRecordResult> toRecordResultPage(PageResp<EmployeePerformanceRecordDto> dtoPage) {
        if (dtoPage == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<EmployeePerformanceRecordResult> resultPage = new PageResp<>();
        resultPage.setPagination(dtoPage.getPagination());
        resultPage.setList(BeanUtil.copyToList(dtoPage.getList(), EmployeePerformanceRecordResult.class));
        return resultPage;
    }

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
