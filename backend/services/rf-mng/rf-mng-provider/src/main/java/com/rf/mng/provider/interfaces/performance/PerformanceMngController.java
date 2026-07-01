package com.rf.mng.provider.interfaces.performance;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.rf.mng.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.mng.provider.application.command.performance.admin.EmployeePerformanceFeedbackHandleCommand;
import com.rf.mng.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import com.rf.mng.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.mng.provider.application.manager.performance.PerformanceMngManager;
import com.rf.mng.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.mng.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.mng.provider.application.result.performance.EmployeePerformanceImportUploadResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.mng.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.rf.mng.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import com.rf.mng.provider.application.result.performance.PerformanceTaskResult;
import com.rf.mng.provider.common.auth.MngModule;
import com.rf.mng.provider.common.auth.MngPermission;
import com.rf.mng.provider.common.oss.OssUploadResult;
import com.rf.mng.provider.common.oss.PrimaryOssUploadService;
import com.rf.mng.provider.interfaces.performance.param.EmployeePerformanceImportCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.admin.EmployeePerformanceAdjustCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.admin.EmployeePerformanceFeedbackHandleCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.admin.EmployeePerformancePageCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.item.EmployeePerformanceImportItemCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.PerformanceTaskCreateCtrlParam;
import com.rf.mng.provider.interfaces.performance.param.PerformanceTaskPageCtrlParam;
import com.rf.mng.provider.interfaces.performance.vo.EmployeePerformanceImportResultVo;
import com.rf.mng.provider.interfaces.performance.vo.EmployeePerformanceImportUploadVo;
import com.rf.mng.provider.interfaces.performance.vo.admin.EmployeePerformanceAdjustVo;
import com.rf.mng.provider.interfaces.performance.vo.admin.EmployeePerformanceRecordVo;
import com.rf.mng.provider.interfaces.performance.vo.item.EmployeePerformanceImportErrorVo;
import com.rf.mng.provider.interfaces.performance.vo.PerformanceTaskVo;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.bo.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 员工绩效管理后台接口。
 */
@RestController
@RequestMapping("/api/performance")
@MngPermission(MngModule.PERFORMANCE)
public class PerformanceMngController {

    /** 导入原始文件 OSS 对象前缀。 */
    private static final String IMPORT_ORIGINAL_OBJECT_PREFIX = "performance/import/original";

    /** 导入失败明细 OSS 对象前缀。 */
    private static final String IMPORT_FAILURE_OBJECT_PREFIX = "performance/import/failure";

    /**
     * 员工绩效管理应用编排。
     */
    @Resource
    private PerformanceMngManager performanceMngManager;

    /** Primary OSS 上传服务。 */
    @Resource
    private PrimaryOssUploadService primaryOssUploadService;

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
     * 上传 Excel 导入员工绩效记录。
     *
     * @param taskId 绩效任务 ID
     * @param file 导入文件
     * @param taskName 绩效任务名称快照
     * @param request HTTP 请求
     * @return 员工绩效导入上传记录
     * @throws IOException 文件读取失败
     */
    @PostMapping(value = "/tasks/{taskId}/records/import-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<EmployeePerformanceImportUploadVo> importRecordsByFile(@PathVariable Long taskId,
                                                                         @RequestPart("file") MultipartFile file,
                                                                         @RequestParam(value = "taskName", required = false) String taskName,
                                                                         HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "导入文件不能为空");
        }
        byte[] fileBytes = file.getBytes();
        List<EmployeePerformanceImportItemCommand> records;
        try {
            records = parseImportFile(fileBytes);
        } catch (RuntimeException exception) {
            EmployeePerformanceImportUploadResult upload = buildFailedUpload(taskId, taskName, file, fileBytes, request,
                    "导入文件解析失败：" + exception.getMessage());
            performanceMngManager.saveImportUpload(upload);
            throw exception;
        }
        EmployeePerformanceImportCommand command = new EmployeePerformanceImportCommand();
        command.setTaskId(taskId);
        command.setRecords(records);
        try {
            EmployeePerformanceImportResult result = performanceMngManager.importRecords(command);
            EmployeePerformanceImportUploadResult upload = buildImportUpload(taskId, taskName, file, fileBytes, records.size(), result, request);
            performanceMngManager.saveImportUpload(upload);
            return Result.success(toImportUploadVo(upload));
        } catch (RuntimeException exception) {
            EmployeePerformanceImportUploadResult upload = buildFailedUpload(taskId, taskName, file, fileBytes, request, exception.getMessage());
            upload.setTotalCount(records.size());
            performanceMngManager.saveImportUpload(upload);
            throw exception;
        }
    }

    /**
     * 查询员工绩效导入上传记录。
     *
     * @param taskId 绩效任务ID
     * @param limit 查询数量
     * @return 上传记录列表
     */
    @GetMapping("/records/import-uploads")
    public Result<List<EmployeePerformanceImportUploadVo>> listImportUploads(@RequestParam(value = "taskId", required = false) Long taskId,
                                                                             @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        List<EmployeePerformanceImportUploadResult> results = performanceMngManager.listImportUploads(taskId, Math.min(Math.max(limit == null ? 50 : limit, 1), 100));
        return Result.success(results.stream().map(this::toImportUploadVo).toList());
    }

    /**
     * 下载导入原始文件。
     *
     * @param uploadId 上传记录ID
     * @param response HTTP 响应
     * @throws IOException 写入响应失败
     */
    @GetMapping("/records/import-uploads/{uploadId}/original")
    public void downloadImportOriginal(@PathVariable Long uploadId, HttpServletResponse response) throws IOException {
        EmployeePerformanceImportUploadResult upload = getImportUploadForDownload(uploadId);
        byte[] content = primaryOssUploadService.getObjectContentByUrl(upload.getOriginalFileUrl());
        writeBinary(response, upload.getOriginalContentType(), upload.getFileName(), content);
    }

    /**
     * 下载导入失败明细文件。
     *
     * @param uploadId 上传记录ID
     * @param response HTTP 响应
     * @throws IOException 写入响应失败
     */
    @GetMapping("/records/import-uploads/{uploadId}/failure")
    public void downloadImportFailure(@PathVariable Long uploadId, HttpServletResponse response) throws IOException {
        EmployeePerformanceImportUploadResult upload = getImportUploadForDownload(uploadId);
        if (!hasText(upload.getFailureFileUrl())) {
            throw new BusinessException(ErrorCode.E999001, "暂无失败明细文件");
        }
        byte[] content = primaryOssUploadService.getObjectContentByUrl(upload.getFailureFileUrl());
        writeBinary(response, "application/vnd.ms-excel;charset=UTF-8", upload.getFailureFileName(), content);
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
        applyExportConfirmScope(query, param == null ? null : param.getExportConfirmScope());
        PageResp<EmployeePerformanceRecordResult> resultPage = performanceMngManager.pageRecords(query);
        writeCsv(response, resultPage == null ? new ArrayList<>() : resultPage.getList());
    }

    /**
     * 删除员工绩效记录。
     *
     * @param recordId 员工绩效记录 ID
     * @return 空结果
     */
    @PostMapping("/records/{recordId}/delete")
    public Result<Void> deleteRecord(@PathVariable Long recordId) {
        performanceMngManager.deleteRecord(recordId);
        return Result.success();
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
        command.setOperatorAdminId((Long) request.getAttribute("adminId"));
        command.setOperatorAdminName(limitText((String) request.getAttribute("adminName"), 64));
        command.setIpAddress(clientIp(request));
        EmployeePerformanceAdjustResult result = performanceMngManager.adjustPerformance(command);
        return Result.success(BeanUtil.copyProperties(result, EmployeePerformanceAdjustVo.class));
    }

    /**
     * 处理反馈且不调整绩效。
     *
     * @param recordId 员工绩效记录 ID
     * @param param 反馈处理参数
     * @return 空结果
     */
    @PostMapping("/records/{recordId}/feedback/unchanged")
    public Result<Void> handleFeedbackUnchanged(@PathVariable Long recordId,
                                                @RequestBody EmployeePerformanceFeedbackHandleCtrlParam param) {
        EmployeePerformanceFeedbackHandleCtrlParam safeParam = param == null ? new EmployeePerformanceFeedbackHandleCtrlParam() : param;
        EmployeePerformanceFeedbackHandleCommand command = BeanUtil.copyProperties(safeParam, EmployeePerformanceFeedbackHandleCommand.class);
        command.setRecordId(recordId);
        performanceMngManager.handleFeedbackUnchanged(command);
        return Result.success();
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
     * 解析员工绩效导入 Excel。
     *
     * @param fileBytes 上传文件内容
     * @return 员工绩效导入明细
     */
    private List<EmployeePerformanceImportItemCommand> parseImportFile(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            return new ArrayList<>();
        }
        List<EmployeePerformanceImportItemCommand> records = new ArrayList<>();
        EasyExcel.read(new ByteArrayInputStream(fileBytes), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                EmployeePerformanceImportItemCommand item = new EmployeePerformanceImportItemCommand();
                item.setRowNo(context.readRowHolder().getRowIndex() + 1);
                item.setEmployeeName(cell(row, 0));
                item.setMobile(cell(row, 1));
                item.setPerformance(cell(row, 2));
                item.setPerformanceExplanation(cell(row, 3));
                item.setEmployeeNo(cell(row, 4));
                item.setProjectDepartment(cell(row, 5));
                item.setPositionName(cell(row, 6));
                if (hasImportValue(item)) {
                    records.add(item);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 读取完成后无额外处理。
            }
        }).headRowNumber(1).sheet().doRead();
        return records;
    }

    /**
     * 构建导入上传记录。
     *
     * @param taskId 绩效任务ID
     * @param taskName 绩效任务名称快照
     * @param file 上传文件
     * @param fileBytes 原始文件内容
     * @param totalCount 总条数
     * @param result 导入结果
     * @param request HTTP 请求
     * @return 上传记录
     */
    private EmployeePerformanceImportUploadResult buildImportUpload(Long taskId,
                                                                    String taskName,
                                                                    MultipartFile file,
                                                                    byte[] fileBytes,
                                                                    int totalCount,
                                                                    EmployeePerformanceImportResult result,
                                                                    HttpServletRequest request) {
        int successCount = result == null || result.getSuccessCount() == null ? 0 : result.getSuccessCount();
        List<EmployeePerformanceImportErrorResult> errors = result == null ? new ArrayList<>() : result.getErrors();
        int failCount = errors == null ? Math.max(totalCount - successCount, 0) : errors.size();
        EmployeePerformanceImportUploadResult upload = baseUpload(taskId, taskName, file, fileBytes, request);
        upload.setTotalCount(totalCount);
        upload.setSuccessCount(successCount);
        upload.setFailCount(failCount);
        upload.setStatus(importStatus(successCount, failCount));
        if (failCount > 0) {
            String failureFileName = failureFileName(file.getOriginalFilename());
            OssUploadResult failureUploadResult = primaryOssUploadService.upload(IMPORT_FAILURE_OBJECT_PREFIX, failureFileName, buildFailureExcel(errors));
            upload.setFailureFileName(failureFileName);
            upload.setFailureFileUrl(failureUploadResult.getUrl());
        }
        return upload;
    }

    /**
     * 构建失败上传记录。
     *
     * @param taskId 绩效任务ID
     * @param taskName 绩效任务名称快照
     * @param file 上传文件
     * @param fileBytes 原始文件内容
     * @param request HTTP 请求
     * @param errorMessage 失败原因
     * @return 上传记录
     */
    private EmployeePerformanceImportUploadResult buildFailedUpload(Long taskId,
                                                                    String taskName,
                                                                    MultipartFile file,
                                                                    byte[] fileBytes,
                                                                    HttpServletRequest request,
                                                                    String errorMessage) {
        EmployeePerformanceImportUploadResult upload = baseUpload(taskId, taskName, file, fileBytes, request);
        upload.setTotalCount(0);
        upload.setSuccessCount(0);
        upload.setFailCount(0);
        upload.setStatus("FAILED");
        upload.setErrorMessage(limitText(errorMessage, 1024));
        return upload;
    }

    /**
     * 构建上传记录基础信息。
     *
     * @param taskId 绩效任务ID
     * @param taskName 绩效任务名称快照
     * @param file 上传文件
     * @param fileBytes 原始文件内容
     * @param request HTTP 请求
     * @return 上传记录
     */
    private EmployeePerformanceImportUploadResult baseUpload(Long taskId,
                                                            String taskName,
                                                            MultipartFile file,
                                                            byte[] fileBytes,
                                                            HttpServletRequest request) {
        EmployeePerformanceImportUploadResult upload = new EmployeePerformanceImportUploadResult();
        upload.setTaskId(taskId);
        upload.setTaskName(limitText(taskName, 255));
        upload.setFileName(limitText(file.getOriginalFilename(), 255));
        upload.setOriginalContentType(file.getContentType());
        OssUploadResult originalUploadResult = primaryOssUploadService.upload(IMPORT_ORIGINAL_OBJECT_PREFIX, file.getOriginalFilename(), fileBytes);
        upload.setOriginalFileUrl(originalUploadResult.getUrl());
        upload.setCreateAdminId((Long) request.getAttribute("adminId"));
        upload.setCreateAdminName(limitText((String) request.getAttribute("adminName"), 64));
        return upload;
    }

    /**
     * 转换导入状态。
     *
     * @param successCount 成功条数
     * @param failCount 失败条数
     * @return 导入状态
     */
    private String importStatus(int successCount, int failCount) {
        if (failCount <= 0) {
            return "SUCCESS";
        }
        if (successCount <= 0) {
            return "FAILED";
        }
        return "PARTIAL_SUCCESS";
    }

    /**
     * 生成失败明细 Excel 文件内容。
     *
     * @param errors 错误明细
     * @return 失败明细文件内容
     */
    private byte[] buildFailureExcel(List<EmployeePerformanceImportErrorResult> errors) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><meta charset=\"utf-8\" /></head><body><table>");
        builder.append("<tr><th>行号</th><th>手机号</th><th>失败原因</th></tr>");
        for (EmployeePerformanceImportErrorResult error : errors == null ? new ArrayList<EmployeePerformanceImportErrorResult>() : errors) {
            builder.append("<tr><td>").append(htmlValue(error.getRowNo())).append("</td><td>")
                    .append(htmlValue(error.getMobile())).append("</td><td>")
                    .append(htmlValue(error.getErrorMessage())).append("</td></tr>");
        }
        builder.append("</table></body></html>");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 转义 HTML 单元格。
     *
     * @param value 单元格值
     * @return 转义后文本
     */
    private String htmlValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * 构建失败明细文件名。
     *
     * @param originalFileName 原始文件名
     * @return 失败明细文件名
     */
    private String failureFileName(String originalFileName) {
        String safeName = originalFileName == null || originalFileName.isBlank() ? "员工绩效导入" : originalFileName;
        int dotIndex = safeName.lastIndexOf('.');
        String prefix = dotIndex > 0 ? safeName.substring(0, dotIndex) : safeName;
        return limitText(prefix + "-失败明细.xls", 255);
    }

    /**
     * 转换上传记录 VO。
     *
     * @param result 应用层上传记录
     * @return HTTP 上传记录
     */
    private EmployeePerformanceImportUploadVo toImportUploadVo(EmployeePerformanceImportUploadResult result) {
        EmployeePerformanceImportUploadVo vo = BeanUtil.copyProperties(result, EmployeePerformanceImportUploadVo.class);
        vo.setHasOriginalFile(hasText(result.getOriginalFileUrl()));
        vo.setHasFailureFile(hasText(result.getFailureFileUrl()));
        return vo;
    }

    /**
     * 判断文本是否存在。
     *
     * @param value 文本
     * @return 是否存在文本
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 获取用于下载的上传记录。
     *
     * @param uploadId 上传记录ID
     * @return 上传记录
     */
    private EmployeePerformanceImportUploadResult getImportUploadForDownload(Long uploadId) {
        EmployeePerformanceImportUploadResult upload = performanceMngManager.getImportUpload(uploadId);
        if (upload == null) {
            throw new BusinessException(ErrorCode.E999001, "上传记录不存在");
        }
        return upload;
    }

    /**
     * 写出二进制文件。
     *
     * @param response HTTP 响应
     * @param contentType 文件类型
     * @param fileName 文件名
     * @param content 文件内容
     * @throws IOException 写入响应失败
     */
    private void writeBinary(HttpServletResponse response, String contentType, String fileName, byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            throw new BusinessException(ErrorCode.E999001, "文件不存在");
        }
        String safeFileName = fileName == null || fileName.isBlank() ? "download" : fileName;
        String encodedFileName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.getOutputStream().write(content);
    }

    /**
     * 截断文本。
     *
     * @param value 原始文本
     * @param maxLength 最大长度
     * @return 截断后文本
     */
    private String limitText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * 读取单元格文本。
     *
     * @param row 行数据
     * @param index 单元格下标
     * @return 单元格文本
     */
    private String cell(Map<Integer, String> row, int index) {
        if (row == null || row.get(index) == null) {
            return null;
        }
        String value = row.get(index).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * 判断导入行是否包含有效内容。
     *
     * @param item 导入行
     * @return 是否包含有效内容
     */
    private boolean hasImportValue(EmployeePerformanceImportItemCommand item) {
        return item.getEmployeeName() != null || item.getMobile() != null || item.getPerformance() != null
                || item.getPerformanceExplanation() != null
                || item.getEmployeeNo() != null || item.getProjectDepartment() != null || item.getPositionName() != null;
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
        List<EmployeePerformanceRecordVo> records = BeanUtil.copyToList(resultPage.getList(), EmployeePerformanceRecordVo.class);
        records.forEach(record -> record.setMobile(maskMobile(record.getMobile())));
        voPage.setList(records);
        return voPage;
    }

    /**
     * 脱敏手机号。
     *
     * @param mobile 手机号
     * @return 脱敏后的手机号
     */
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
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
        appendCsvLine(builder, List.of("绩效描述", "姓名", "手机号", "工号", "项目/部门", "岗位", "绩效", "绩效说明",
                "是否确认", "确认状态", "是否有反馈", "反馈状态", "反馈内容", "处理意见", "处理人", "最终绩效"));
        for (EmployeePerformanceRecordResult record : records) {
            appendCsvLine(builder, Arrays.asList(
                    record.getPerformanceDescription(),
                    record.getEmployeeName(),
                    record.getMobile(),
                    record.getEmployeeNo(),
                    record.getProjectDepartment(),
                    record.getPositionName(),
                    record.getPerformance(),
                    record.getPerformanceExplanation(),
                    confirmedText(record.getConfirmStatus()),
                    confirmStatusText(record.getConfirmStatus()),
                    feedbackText(record.getFeedbackStatus()),
                    feedbackStatusText(record.getFeedbackStatus()),
                    record.getFeedbackContent(),
                    record.getFeedbackHandleOpinion(),
                    record.getFeedbackHandleAdminName(),
                    record.getPerformance()));
        }
        response.getWriter().write(builder.toString());
    }

    /**
     * 应用导出确认范围。
     *
     * @param query 查询条件
     * @param exportConfirmScope 导出确认范围
     */
    private void applyExportConfirmScope(EmployeePerformancePageQuery query, String exportConfirmScope) {
        if (query == null || exportConfirmScope == null || exportConfirmScope.isBlank() || "ALL".equals(exportConfirmScope)) {
            return;
        }
        query.setConfirmStatus(null);
        if ("CONFIRMED".equals(exportConfirmScope)) {
            query.setConfirmStatusList(List.of("CONFIRMED", "SECOND_CONFIRMED", "AUTO_CONFIRMED", "SECOND_AUTO_CONFIRMED"));
            return;
        }
        if ("UNCONFIRMED".equals(exportConfirmScope)) {
            query.setConfirmStatusList(List.of("PENDING_CONFIRM", "FEEDBACK_SUBMITTED", "PENDING_SECOND_CONFIRM"));
        }
    }

    /**
     * 转换是否确认文案。
     *
     * @param status 确认状态编码
     * @return 是否确认文案
     */
    private String confirmedText(String status) {
        if ("CONFIRMED".equals(status) || "SECOND_CONFIRMED".equals(status)
                || "AUTO_CONFIRMED".equals(status) || "SECOND_AUTO_CONFIRMED".equals(status)) {
            return "是";
        }
        return "否";
    }

    /**
     * 转换是否有反馈文案。
     *
     * @param status 反馈状态编码
     * @return 是否有反馈文案
     */
    private String feedbackText(String status) {
        if (status == null || status.isBlank() || "NONE".equals(status)) {
            return "否";
        }
        return "是";
    }

    /**
     * 转换确认状态文案。
     *
     * @param status 确认状态编码
     * @return 确认状态文案
     */
    private String confirmStatusText(String status) {
        if ("PENDING_CONFIRM".equals(status)) {
            return "待确认";
        }
        if ("CONFIRMED".equals(status)) {
            return "已确认";
        }
        if ("AUTO_CONFIRMED".equals(status)) {
            return "超时自动确认";
        }
        if ("FEEDBACK_SUBMITTED".equals(status)) {
            return "已反馈";
        }
        if ("PENDING_SECOND_CONFIRM".equals(status)) {
            return "待二次确认";
        }
        if ("SECOND_CONFIRMED".equals(status)) {
            return "二次已确认";
        }
        if ("SECOND_AUTO_CONFIRMED".equals(status)) {
            return "二次超时自动确认";
        }
        return status;
    }

    /**
     * 转换反馈状态文案。
     *
     * @param status 反馈状态编码
     * @return 反馈状态文案
     */
    private String feedbackStatusText(String status) {
        if ("NONE".equals(status)) {
            return "无反馈";
        }
        if ("PENDING".equals(status)) {
            return "待处理";
        }
        if ("HANDLED_ADJUSTED".equals(status)) {
            return "已调整";
        }
        if ("HANDLED_UNCHANGED".equals(status)) {
            return "已处理未调整";
        }
        return status;
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
