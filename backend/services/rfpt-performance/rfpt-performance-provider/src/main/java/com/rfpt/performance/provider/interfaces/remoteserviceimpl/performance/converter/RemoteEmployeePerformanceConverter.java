package com.rfpt.performance.provider.interfaces.remoteserviceimpl.performance.converter;

import cn.hutool.core.bean.BeanUtil;
import com.rfpt.performance.api.dto.performance.EmployeePerformanceImportResultDto;
import com.rfpt.performance.api.dto.performance.admin.EmployeePerformanceAdjustDto;
import com.rfpt.performance.api.dto.performance.admin.EmployeePerformanceRecordDto;
import com.rfpt.performance.api.dto.performance.item.EmployeePerformanceImportErrorDto;
import com.rfpt.performance.api.param.performance.EmployeePerformanceImportParam;
import com.rfpt.performance.api.param.performance.admin.EmployeePerformanceAdjustParam;
import com.rfpt.performance.api.param.performance.item.EmployeePerformanceImportItemParam;
import com.rfpt.performance.api.query.performance.EmployeePerformancePageParam;
import com.rfpt.performance.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rfpt.performance.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rfpt.performance.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import com.rfpt.performance.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rfpt.performance.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rfpt.performance.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rfpt.performance.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.rfpt.performance.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import com.zy.common.core.bo.PageResp;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工绩效 RPC 对象转换器。
 */
public final class RemoteEmployeePerformanceConverter {

    /**
     * 隐藏工具类构造方法。
     */
    private RemoteEmployeePerformanceConverter() {
    }

    /**
     * 转换导入命令。
     *
     * @param param RPC 导入入参
     * @return 应用层导入命令
     */
    public static EmployeePerformanceImportCommand toImportCommand(EmployeePerformanceImportParam param) {
        EmployeePerformanceImportParam safeParam = param == null ? new EmployeePerformanceImportParam() : param;
        EmployeePerformanceImportCommand command = BeanUtil.copyProperties(safeParam, EmployeePerformanceImportCommand.class);
        command.setRecords(toItemCommands(safeParam.getRecords()));
        return command;
    }

    /**
     * 转换导入返回对象。
     *
     * @param result 应用层导入结果
     * @return RPC 导入结果
     */
    public static EmployeePerformanceImportResultDto toImportDto(EmployeePerformanceImportResult result) {
        EmployeePerformanceImportResult safeResult = result == null ? new EmployeePerformanceImportResult() : result;
        EmployeePerformanceImportResultDto dto = BeanUtil.copyProperties(safeResult, EmployeePerformanceImportResultDto.class);
        dto.setErrors(toErrorDtos(safeResult.getErrors()));
        return dto;
    }

    /**
     * 转换调整命令。
     *
     * @param param RPC 调整入参
     * @return 应用层调整命令
     */
    public static EmployeePerformanceAdjustCommand toAdjustCommand(EmployeePerformanceAdjustParam param) {
        return BeanUtil.copyProperties(param, EmployeePerformanceAdjustCommand.class);
    }

    /**
     * 转换调整返回对象。
     *
     * @param result 应用层调整结果
     * @return RPC 调整结果
     */
    public static EmployeePerformanceAdjustDto toAdjustDto(EmployeePerformanceAdjustResult result) {
        return BeanUtil.copyProperties(result, EmployeePerformanceAdjustDto.class);
    }

    /**
     * 转换分页查询条件。
     *
     * @param param RPC 分页查询条件
     * @return 应用层分页查询条件
     */
    public static EmployeePerformancePageQuery toPageQuery(EmployeePerformancePageParam param) {
        return BeanUtil.copyProperties(param, EmployeePerformancePageQuery.class);
    }

    /**
     * 转换分页返回对象。
     *
     * @param pageResp 应用层分页结果
     * @return RPC 分页结果
     */
    public static PageResp<EmployeePerformanceRecordDto> toRecordDtoPage(PageResp<EmployeePerformanceRecordResult> pageResp) {
        if (pageResp == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<EmployeePerformanceRecordDto> dtoPage = new PageResp<>();
        dtoPage.setPagination(pageResp.getPagination());
        dtoPage.setList(BeanUtil.copyToList(pageResp.getList(), EmployeePerformanceRecordDto.class));
        return dtoPage;
    }

    /**
     * 转换导入明细命令。
     *
     * @param items RPC 导入明细
     * @return 应用层导入明细
     */
    private static List<EmployeePerformanceImportItemCommand> toItemCommands(List<EmployeePerformanceImportItemParam> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(items, EmployeePerformanceImportItemCommand.class);
    }

    /**
     * 转换导入错误明细。
     *
     * @param errors 应用层错误明细
     * @return RPC 错误明细
     */
    private static List<EmployeePerformanceImportErrorDto> toErrorDtos(List<EmployeePerformanceImportErrorResult> errors) {
        if (errors == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(errors, EmployeePerformanceImportErrorDto.class);
    }
}
