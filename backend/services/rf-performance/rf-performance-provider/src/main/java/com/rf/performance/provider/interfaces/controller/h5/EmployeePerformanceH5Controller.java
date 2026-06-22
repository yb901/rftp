package com.rf.performance.provider.interfaces.controller.h5;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.command.performance.h5.PerformanceH5ConfirmCommand;
import com.rf.performance.provider.application.command.performance.h5.PerformanceH5FeedbackCommand;
import com.rf.performance.provider.application.manager.performance.h5.EmployeePerformanceH5Manager;
import com.rf.performance.provider.application.result.performance.h5.EmployeePerformanceH5Result;
import com.rf.performance.provider.common.web.PerformanceH5RequestContext;
import com.rf.performance.provider.interfaces.controller.h5.param.PerformanceH5ConfirmCtrlParam;
import com.rf.performance.provider.interfaces.controller.h5.param.PerformanceH5FeedbackCtrlParam;
import com.rf.performance.provider.interfaces.controller.h5.vo.EmployeePerformanceRecordVo;
import com.zy.common.core.bo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import javax.annotation.Resource;
import java.util.List;

/**
 * 员工绩效 H5 控制器。
 */
@RestController
@RequestMapping("/api/performance/h5/records")
public class EmployeePerformanceH5Controller {

    /**
     * 员工绩效 H5 应用编排。
     */
    @Resource
    private EmployeePerformanceH5Manager employeePerformanceH5Manager;

    /**
     * 员工绩效 H5 请求上下文。
     */
    @Resource
    private PerformanceH5RequestContext performanceH5RequestContext;

    /**
     * 查询当前员工绩效记录。
     *
     * @param request HTTP 请求
     * @return 当前员工绩效记录列表
     */
    @GetMapping("/mine")
    public Result<List<EmployeePerformanceRecordVo>> listMine(HttpServletRequest request) {
        String mobile = performanceH5RequestContext.requireMobile(request);
        List<EmployeePerformanceH5Result> results = employeePerformanceH5Manager.listMine(mobile);
        return Result.success(BeanUtil.copyToList(results, EmployeePerformanceRecordVo.class));
    }

    /**
     * 确认绩效。
     *
     * @param recordId 员工绩效记录 ID
     * @param param 确认参数
     * @param request HTTP 请求
     * @return 空结果
     */
    @PostMapping("/{recordId}/confirm")
    public Result<Void> confirm(@PathVariable Long recordId,
                                @RequestBody PerformanceH5ConfirmCtrlParam param,
                                HttpServletRequest request) {
        PerformanceH5ConfirmCommand command = new PerformanceH5ConfirmCommand();
        command.setRecordId(recordId);
        command.setMobile(performanceH5RequestContext.requireMobile(request));
        command.setSmsCode(param == null ? null : param.getSmsCode());
        command.setIpAddress(performanceH5RequestContext.clientIp(request));
        command.setUserAgent(performanceH5RequestContext.userAgent(request));
        employeePerformanceH5Manager.confirm(command);
        return Result.success();
    }

    /**
     * 提交绩效反馈。
     *
     * @param recordId 员工绩效记录 ID
     * @param param 反馈参数
     * @param request HTTP 请求
     * @return 空结果
     */
    @PostMapping("/{recordId}/feedback")
    public Result<Void> feedback(@PathVariable Long recordId,
                                 @RequestBody PerformanceH5FeedbackCtrlParam param,
                                 HttpServletRequest request) {
        PerformanceH5FeedbackCommand command = new PerformanceH5FeedbackCommand();
        command.setRecordId(recordId);
        command.setMobile(performanceH5RequestContext.requireMobile(request));
        command.setFeedbackContent(param == null ? null : param.getFeedbackContent());
        command.setIpAddress(performanceH5RequestContext.clientIp(request));
        command.setUserAgent(performanceH5RequestContext.userAgent(request));
        employeePerformanceH5Manager.feedback(command);
        return Result.success();
    }
}
