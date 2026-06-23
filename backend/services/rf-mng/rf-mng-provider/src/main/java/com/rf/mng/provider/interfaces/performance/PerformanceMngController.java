package com.rf.mng.provider.interfaces.performance;

import cn.hutool.core.bean.BeanUtil;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.bo.Result;
import com.rf.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.mng.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import com.rf.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.mng.provider.application.manager.performance.PerformanceMngManager;
import com.rf.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.rf.mng.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import com.rf.mng.provider.application.result.performance.PerformanceTaskResult;
import com.rf.mng.provider.interfaces.performance.param.EmployeePerformanceImportCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.admin.EmployeePerformanceAdjustCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.admin.EmployeePerformancePageCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.item.EmployeePerformanceImportItemCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.PerformanceTaskCreateCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.PerformanceTaskPageCtrlParam;
import com.rf.mng.provider.interfaces.performance.vo.EmployeePerformanceImportResultVo;
import com.rf.mng.provider.interfaces.performance.vo.admin.EmployeePerformanceAdjustVo;
import com.rf.mng.provider.interfaces.performance.vo.admin.EmployeePerformanceRecordVo;
import com.rf.mng.provider.interfaces.performance.vo.item.EmployeePerformanceImportErrorVo;
import com.rf.mng.provider.interfaces.performance.vo.PerformanceTaskVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 员工绩效管理后台接口。
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceMngController {

    /**
     * 员工绩效管理应用编排。
     */
    @Resource
    private PerformanceMngManager performanceMngManager;

    /**
     * 创建绩效任务。
     *
     * @param param 绩效任务创建参数
     * @return 绩效任务信息
     */
    @PostMapping("/tasks")
    public Result<PerformanceTaskVo> createTask(@RequestBody PerformanceTaskCreateCtrlParam param) {
        PerformanceTaskCreateCommand command = BeanUtil.copyProperties(param, PerformanceTaskCreateCommand.class);
        PerformanceTaskResult result = performanceMngManager.createTask(command);
        return Result.success(BeanUtil.copyProperties(result, PerformanceTaskVo.class));
    }

    /**
     * 分页查询绩效任务。
     *
     * @param param 分页查询参数
     * @return 绩效任务分页结果
     */
    @GetMapping("/tasks")
    public Result<PageResp<PerformanceTaskVo>> pageTasks(PerformanceTaskPageCtrlParam param) {
        PerformanceTaskPageQuery query = BeanUtil.copyProperties(param, PerformanceTaskPageQuery.class);
        PageResp<PerformanceTaskResult> resultPage = performanceMngManager.pageTasks(query);
        return Result.success(toTaskVoPage(resultPage));
    }

    /**
     * 启用绩效任务。
     *
     * @param taskId 绩效任务 ID
     * @return 启用结果
     */
    @PostMapping("/tasks/{taskId}/enable")
    public Result<Void> enableTask(@PathVariable Long taskId) {
        performanceMngManager.enableTask(taskId);
        return Result.success();
    }

    /**
     * 停用绩效任务。
     *
     * @param taskId 绩效任务 ID
     * @return 停用结果
     */
    @PostMapping("/tasks/{taskId}/disable")
    public Result<Void> disableTask(@PathVariable Long taskId) {
        performanceMngManager.disableTask(taskId);
        return Result.success();
    }

    /**
     * 删除绩效任务。
     *
     * @param taskId 绩效任务 ID
     * @return 删除结果
     */
    @PostMapping("/tasks/{taskId}/delete")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        performanceMngManager.deleteTask(taskId);
        return Result.success();
    }

    /**
     * 导入员工绩效记录。
     *
     * @param taskId 绩效任务 ID
     * @param param 员工绩效导入参数
     * @return 员工绩效导入结果
     */
    @PostMapping("/tasks/{taskId}/records/import")
    public Result<EmployeePerformanceImportResultVo> importRecords(@PathVariable Long taskId,
                                                                   @RequestBody EmployeePerformanceImportCtrlParam param) {
        EmployeePerformanceImportCtrlParam safeParam = param == null ? new EmployeePerformanceImportCtrlParam() : param;
        EmployeePerformanceImportCommand command = BeanUtil.copyProperties(safeParam, EmployeePerformanceImportCommand.class);
        command.setRecords(toImportItemCommands(safeParam.getRecords()));
        command.setTaskId(taskId);
        EmployeePerformanceImportResult result = performanceMngManager.importRecords(command);
        EmployeePerformanceImportResultVo vo = BeanUtil.copyProperties(result, EmployeePerformanceImportResultVo.class);
        vo.setErrors(toImportErrorVos(result == null ? null : result.getErrors()));
        return Result.success(vo);
    }

    /**
     * 分页查询员工绩效记录。
     *
     * @param param 分页查询参数
     * @return 员工绩效分页结果
     */
    @GetMapping("/records")
    public Result<PageResp<EmployeePerformanceRecordVo>> pageRecords(EmployeePerformancePageCtrlParam param) {
        EmployeePerformancePageQuery query = BeanUtil.copyProperties(param, EmployeePerformancePageQuery.class);
        PageResp<EmployeePerformanceRecordResult> resultPage = performanceMngManager.pageRecords(query);
        return Result.success(toRecordVoPage(resultPage));
    }

    /**
     * 导出员工绩效记录。
     *
     * @param param 导出查询参数
     * @param response HTTP 响应
     * @throws IOException 写入响应失败
     */
    @GetMapping("/records/export")
    public void exportRecords(EmployeePerformancePageCtrlParam param,
                              HttpServletResponse response) throws IOException {
        EmployeePerformancePageQuery query = BeanUtil.copyProperties(param, EmployeePerformancePageQuery.class);
        query.setPage(1);
        query.setSize(10000);
        PageResp<EmployeePerformanceRecordResult> resultPage = performanceMngManager.pageRecords(query);
        writeCsv(response, resultPage == null ? new ArrayList<>() : resultPage.getList());
    }

    /**
     * 调整员工绩效。
     *
     * @param recordId 员工绩效记录 ID
     * @param param 调整参数
     * @param request HTTP 请求
     * @return 调整结果
     */
    @PostMapping("/records/{recordId}/adjust")
    public Result<EmployeePerformanceAdjustVo> adjustPerformance(@PathVariable Long recordId,
                                                                 @RequestBody EmployeePerformanceAdjustCtrlParam param,
                                                                 HttpServletRequest request) {
        EmployeePerformanceAdjustCtrlParam safeParam = param == null ? new EmployeePerformanceAdjustCtrlParam() : param;
        EmployeePerformanceAdjustCommand command = BeanUtil.copyProperties(safeParam, EmployeePerformanceAdjustCommand.class);
        command.setRecordId(recordId);
        command.setIpAddress(clientIp(request));
        EmployeePerformanceAdjustResult result = performanceMngManager.adjustPerformance(command);
        return Result.success(BeanUtil.copyProperties(result, EmployeePerformanceAdjustVo.class));
    }

    /**
     * 转换员工绩效导入明细命令。
     *
     * @param records HTTP 导入明细
     * @return 应用层导入明细
     */
    private List<EmployeePerformanceImportItemCommand> toImportItemCommands(List<EmployeePerformanceImportItemCtrlParam> records) {
        if (records == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(records, EmployeePerformanceImportItemCommand.class);
    }

    /**
     * 转换员工绩效导入错误 VO。
     *
     * @param errors 应用层错误明细
     * @return HTTP 错误明细
     */
    private List<EmployeePerformanceImportErrorVo> toImportErrorVos(List<EmployeePerformanceImportErrorResult> errors) {
        if (errors == null) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(errors, EmployeePerformanceImportErrorVo.class);
    }

    /**
     * 转换员工绩效分页 VO。
     *
     * @param resultPage 应用层分页结果
     * @return HTTP 分页结果
     */
    private PageResp<EmployeePerformanceRecordVo> toRecordVoPage(PageResp<EmployeePerformanceRecordResult> resultPage) {
        if (resultPage == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<EmployeePerformanceRecordVo> voPage = new PageResp<>();
        voPage.setPagination(resultPage.getPagination());
        voPage.setList(BeanUtil.copyToList(resultPage.getList(), EmployeePerformanceRecordVo.class));
        return voPage;
    }

    /**
     * 转换绩效任务分页 VO。
     *
     * @param resultPage 应用层分页结果
     * @return HTTP 分页结果
     */
    private PageResp<PerformanceTaskVo> toTaskVoPage(PageResp<PerformanceTaskResult> resultPage) {
        if (resultPage == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<PerformanceTaskVo> voPage = new PageResp<>();
        voPage.setPagination(resultPage.getPagination());
        voPage.setList(BeanUtil.copyToList(resultPage.getList(), PerformanceTaskVo.class));
        return voPage;
    }

    /**
     * 写出员工绩效 CSV。
     *
     * @param response HTTP 响应
     * @param records 员工绩效记录
     * @throws IOException 写入响应失败
     */
    private void writeCsv(HttpServletResponse response, List<EmployeePerformanceRecordResult> records) throws IOException {
        String fileName = URLEncoder.encode("员工绩效记录.csv", StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        appendCsvLine(builder, List.of("绩效描述", "姓名", "手机号", "工号", "项目/部门", "岗位", "绩效", "确认状态", "反馈状态", "反馈内容", "处理意见", "处理人"));
        for (EmployeePerformanceRecordResult record : records) {
            appendCsvLine(builder, Arrays.asList(
                    record.getPerformanceDescription(),
                    record.getEmployeeName(),
                    record.getMobile(),
                    record.getEmployeeNo(),
                    record.getProjectDepartment(),
                    record.getPositionName(),
                    record.getPerformance(),
                    record.getConfirmStatus(),
                    record.getFeedbackStatus(),
                    record.getFeedbackContent(),
                    record.getFeedbackHandleOpinion(),
                    record.getFeedbackHandleAdminName()));
        }
        response.getWriter().write(builder.toString());
    }

    /**
     * 追加 CSV 行。
     *
     * @param builder CSV 文本
     * @param values 字段值
     */
    private void appendCsvLine(StringBuilder builder, List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(csvValue(values.get(index)));
        }
        builder.append('\n');
    }

    /**
     * 转义 CSV 字段。
     *
     * @param value 字段值
     * @return 转义后的字段值
     */
    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * 获取客户端 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
