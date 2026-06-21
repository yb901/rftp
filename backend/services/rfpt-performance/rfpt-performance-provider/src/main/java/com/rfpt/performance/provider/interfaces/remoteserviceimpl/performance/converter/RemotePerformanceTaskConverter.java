package com.rfpt.performance.provider.interfaces.remoteserviceimpl.performance.converter;

import cn.hutool.core.bean.BeanUtil;
import com.rfpt.performance.api.dto.performance.PerformanceTaskDto;
import com.rfpt.performance.api.param.performance.PerformanceTaskCreateParam;
import com.rfpt.performance.api.query.performance.PerformanceTaskPageParam;
import com.rfpt.performance.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rfpt.performance.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rfpt.performance.provider.application.result.performance.PerformanceTaskResult;
import com.zy.common.core.bo.PageResp;

import java.util.ArrayList;

/**
 * 绩效任务 RPC 对象转换器。
 */
public final class RemotePerformanceTaskConverter {

    /**
     * 隐藏工具类构造方法。
     */
    private RemotePerformanceTaskConverter() {
    }

    /**
     * 转换创建命令。
     *
     * @param param RPC 创建入参
     * @return 应用层创建命令
     */
    public static PerformanceTaskCreateCommand toCreateCommand(PerformanceTaskCreateParam param) {
        return BeanUtil.copyProperties(param, PerformanceTaskCreateCommand.class);
    }

    /**
     * 转换 RPC 返回对象。
     *
     * @param result 应用层结果
     * @return RPC 返回对象
     */
    public static PerformanceTaskDto toDto(PerformanceTaskResult result) {
        return BeanUtil.copyProperties(result, PerformanceTaskDto.class);
    }

    /**
     * 转换分页查询条件。
     *
     * @param param RPC 分页查询条件
     * @return 应用层分页查询条件
     */
    public static PerformanceTaskPageQuery toPageQuery(PerformanceTaskPageParam param) {
        return BeanUtil.copyProperties(param, PerformanceTaskPageQuery.class);
    }

    /**
     * 转换分页返回对象。
     *
     * @param pageResp 应用层分页
     * @return RPC 分页
     */
    public static PageResp<PerformanceTaskDto> toDtoPage(PageResp<PerformanceTaskResult> pageResp) {
        if (pageResp == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<PerformanceTaskDto> dtoPage = new PageResp<>();
        dtoPage.setPagination(pageResp.getPagination());
        dtoPage.setList(BeanUtil.copyToList(pageResp.getList(), PerformanceTaskDto.class));
        return dtoPage;
    }
}
