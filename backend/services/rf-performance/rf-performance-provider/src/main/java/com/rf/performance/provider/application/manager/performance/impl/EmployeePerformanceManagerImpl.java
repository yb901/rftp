package com.rf.performance.provider.application.manager.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.command.performance.EmployeePerformanceImportCommand;
import com.rf.performance.provider.application.command.performance.admin.EmployeePerformanceAdjustCommand;
import com.rf.performance.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import com.rf.performance.provider.application.manager.performance.EmployeePerformanceManager;
import com.rf.performance.provider.application.port.persistence.performance.EmployeePerformanceRecordPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.PerformanceTaskPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.data.EmployeePerformanceRecordData;
import com.rf.performance.provider.application.port.persistence.performance.data.admin.EmployeePerformanceAdjustLogData;
import com.rf.performance.provider.application.port.persistence.performance.record.PerformanceTaskRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceAdminRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceFeedbackRecord;
import com.rf.performance.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.performance.provider.application.result.performance.EmployeePerformanceImportResult;
import com.rf.performance.provider.application.result.performance.admin.EmployeePerformanceAdjustResult;
import com.rf.performance.provider.application.result.performance.admin.EmployeePerformanceRecordResult;
import com.rf.performance.provider.application.result.performance.item.EmployeePerformanceImportErrorResult;
import com.rf.performance.provider.domain.performance.PerformanceConfirmStatus;
import com.rf.performance.provider.domain.performance.PerformanceFeedbackStatus;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.common.core.bo.PageResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 员工绩效记录应用编排实现。
 */
@Service
public class EmployeePerformanceManagerImpl implements EmployeePerformanceManager {

    /**
     * 手机号格式。
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");

    /**
     * 绩效任务持久化端口。
     */
    @Resource
    private PerformanceTaskPersistencePort performanceTaskPersistencePort;

    /**
     * 员工绩效记录持久化端口。
     */
    @Resource
    private EmployeePerformanceRecordPersistencePort employeePerformanceRecordPersistencePort;

    /**
     * 导入员工绩效记录。
     *
     * @param command 员工绩效导入命令
     * @return 员工绩效导入结果
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public EmployeePerformanceImportResult importRecords(EmployeePerformanceImportCommand command) {
        EmployeePerformanceImportCommand safeCommand = command == null ? new EmployeePerformanceImportCommand() : command;
        validateBaseCommand(safeCommand);
        requireTask(safeCommand.getTaskId());
        List<EmployeePerformanceImportErrorResult> errors = validateRows(safeCommand);
        if (!errors.isEmpty()) {
            return failedResult(errors);
        }
        List<String> mobiles = collectMobiles(safeCommand.getRecords());
        List<String> existingMobiles = employeePerformanceRecordPersistencePort.listExistingMobiles(safeCommand.getTaskId(), mobiles);
        errors.addAll(toExistingErrors(safeCommand.getRecords(), existingMobiles));
        if (!errors.isEmpty()) {
            return failedResult(errors);
        }
        List<EmployeePerformanceRecordData> records = toRecordData(safeCommand);
        int insertedCount = employeePerformanceRecordPersistencePort.batchInsert(records);
        performanceTaskPersistencePort.increaseTotalCount(safeCommand.getTaskId(), insertedCount);
        return successResult(insertedCount);
    }

    /**
     * 分页查询员工绩效记录。
     *
     * @param query 查询条件
     * @return 员工绩效记录分页
     */
    @Override
    public PageResp<EmployeePerformanceRecordResult> pageRecords(EmployeePerformancePageQuery query) {
        EmployeePerformancePageQuery safeQuery = query == null ? new EmployeePerformancePageQuery() : query;
        long total = employeePerformanceRecordPersistencePort.count(safeQuery);
        List<EmployeePerformanceAdminRecord> records = employeePerformanceRecordPersistencePort.page(safeQuery);
        List<EmployeePerformanceRecordResult> results = BeanUtil.copyToList(records, EmployeePerformanceRecordResult.class);
        fillTaskDescriptions(results);
        fillFeedback(results);
        return PageResp.of(results, total, safeQuery.getPage(), safeQuery.getSize());
    }

    /**
     * 调整员工绩效。
     *
     * @param command 员工绩效调整命令
     * @return 员工绩效调整结果
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public EmployeePerformanceAdjustResult adjustPerformance(EmployeePerformanceAdjustCommand command) {
        EmployeePerformanceAdjustCommand safeCommand = command == null ? new EmployeePerformanceAdjustCommand() : command;
        validateAdjustCommand(safeCommand);
        EmployeePerformanceAdminRecord record = employeePerformanceRecordPersistencePort.getById(safeCommand.getRecordId());
        if (record == null || record.getId() == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效记录不存在");
        }
        EmployeePerformanceAdjustLogData logData = toAdjustLogData(safeCommand, record);
        if (!employeePerformanceRecordPersistencePort.insertAdjustLog(logData)) {
            throw new BusinessException(ErrorCode.E999002, "绩效调整留痕失败");
        }
        if (!employeePerformanceRecordPersistencePort.updatePerformanceForSecondConfirm(record.getId(), safeCommand.getAfterPerformance())) {
            throw new BusinessException(ErrorCode.E999002, "绩效调整失败");
        }
        employeePerformanceRecordPersistencePort.markFeedbackAdjusted(record.getId(), safeCommand.getAdjustReason(),
                safeCommand.getOperatorAdminId(), safeCommand.getOperatorAdminName());
        EmployeePerformanceAdjustResult result = new EmployeePerformanceAdjustResult();
        result.setRecordId(record.getId());
        result.setBeforePerformance(record.getPerformance());
        result.setAfterPerformance(safeCommand.getAfterPerformance());
        return result;
    }

    /**
     * 校验导入基础命令。
     *
     * @param command 员工绩效导入命令
     */
    private void validateBaseCommand(EmployeePerformanceImportCommand command) {
        if (command.getTaskId() == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务ID不能为空");
        }
        if (command.getRecords() == null || command.getRecords().isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "导入明细不能为空");
        }
    }

    /**
     * 批量回填绩效任务描述。
     *
     * @param records 员工绩效记录
     */
    private void fillTaskDescriptions(List<EmployeePerformanceRecordResult> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> taskIds = records.stream()
                .map(EmployeePerformanceRecordResult::getTaskId)
                .distinct()
                .toList();
        List<PerformanceTaskRecord> tasks = performanceTaskPersistencePort.listByIds(taskIds);
        Map<Long, String> taskDescriptionMap = new HashMap<>();
        for (PerformanceTaskRecord task : tasks) {
            taskDescriptionMap.put(task.getId(), task.getPerformanceDescription());
        }
        for (EmployeePerformanceRecordResult record : records) {
            record.setPerformanceDescription(taskDescriptionMap.get(record.getTaskId()));
        }
    }

    /**
     * 批量回填绩效反馈内容。
     *
     * @param records 员工绩效记录
     */
    private void fillFeedback(List<EmployeePerformanceRecordResult> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> recordIds = records.stream()
                .map(EmployeePerformanceRecordResult::getId)
                .distinct()
                .toList();
        List<EmployeePerformanceFeedbackRecord> feedbackRecords = employeePerformanceRecordPersistencePort.listFeedbackByRecordIds(recordIds);
        Map<Long, EmployeePerformanceFeedbackRecord> feedbackMap = new HashMap<>();
        for (EmployeePerformanceFeedbackRecord feedbackRecord : feedbackRecords) {
            feedbackMap.put(feedbackRecord.getRecordId(), feedbackRecord);
        }
        for (EmployeePerformanceRecordResult record : records) {
            EmployeePerformanceFeedbackRecord feedbackRecord = feedbackMap.get(record.getId());
            if (feedbackRecord == null) {
                continue;
            }
            record.setFeedbackContent(feedbackRecord.getFeedbackContent());
            record.setFeedbackHandleOpinion(feedbackRecord.getHandleOpinion());
            record.setFeedbackHandleAdminName(feedbackRecord.getHandleAdminName());
        }
    }

    /**
     * 校验绩效调整命令。
     *
     * @param command 员工绩效调整命令
     */
    private void validateAdjustCommand(EmployeePerformanceAdjustCommand command) {
        if (command.getRecordId() == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效记录ID不能为空");
        }
        if (StringUtils.isBlank(command.getAfterPerformance())) {
            throw new BusinessException(ErrorCode.E999001, "调整后绩效不能为空");
        }
    }

    /**
     * 转换调整留痕写入数据。
     *
     * @param command 员工绩效调整命令
     * @param record 员工绩效记录
     * @return 调整留痕写入数据
     */
    private EmployeePerformanceAdjustLogData toAdjustLogData(EmployeePerformanceAdjustCommand command,
                                                             EmployeePerformanceAdminRecord record) {
        EmployeePerformanceAdjustLogData data = new EmployeePerformanceAdjustLogData();
        data.setTaskId(record.getTaskId());
        data.setRecordId(record.getId());
        data.setMobile(record.getMobile());
        data.setBeforePerformance(record.getPerformance());
        data.setAfterPerformance(command.getAfterPerformance());
        data.setAdjustReason(command.getAdjustReason());
        data.setOperatorAdminId(command.getOperatorAdminId());
        data.setOperatorAdminName(command.getOperatorAdminName());
        data.setOperatorMobile(command.getOperatorMobile());
        data.setIpAddress(command.getIpAddress());
        return data;
    }

    /**
     * 要求绩效任务存在。
     *
     * @param taskId 绩效任务 ID
     */
    private void requireTask(Long taskId) {
        PerformanceTaskRecord task = performanceTaskPersistencePort.getById(taskId);
        if (task == null || task.getId() == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务不存在");
        }
    }

    /**
     * 校验导入行。
     *
     * @param command 员工绩效导入命令
     * @return 错误明细
     */
    private List<EmployeePerformanceImportErrorResult> validateRows(EmployeePerformanceImportCommand command) {
        List<EmployeePerformanceImportErrorResult> errors = new ArrayList<>();
        Map<String, Integer> mobileFirstRowMap = new HashMap<>();
        for (EmployeePerformanceImportItemCommand row : command.getRecords()) {
            EmployeePerformanceImportItemCommand safeRow = row == null ? new EmployeePerformanceImportItemCommand() : row;
            validateRequiredFields(errors, safeRow);
            validateDuplicateMobile(errors, mobileFirstRowMap, safeRow);
        }
        return errors;
    }

    /**
     * 校验导入必填字段。
     *
     * @param errors 错误明细
     * @param row 导入行
     */
    private void validateRequiredFields(List<EmployeePerformanceImportErrorResult> errors,
                                        EmployeePerformanceImportItemCommand row) {
        if (StringUtils.isBlank(row.getEmployeeName())) {
            errors.add(error(row, "员工姓名不能为空"));
        }
        if (StringUtils.isBlank(row.getMobile())) {
            errors.add(error(row, "手机号不能为空"));
            return;
        }
        if (!MOBILE_PATTERN.matcher(row.getMobile()).matches()) {
            errors.add(error(row, "手机号格式不正确"));
        }
        if (StringUtils.isBlank(row.getPerformance())) {
            errors.add(error(row, "绩效不能为空"));
        }
    }

    /**
     * 校验导入手机号重复。
     *
     * @param errors 错误明细
     * @param mobileFirstRowMap 手机号首次行号映射
     * @param row 导入行
     */
    private void validateDuplicateMobile(List<EmployeePerformanceImportErrorResult> errors,
                                         Map<String, Integer> mobileFirstRowMap,
                                         EmployeePerformanceImportItemCommand row) {
        if (StringUtils.isBlank(row.getMobile())) {
            return;
        }
        Integer firstRowNo = mobileFirstRowMap.putIfAbsent(row.getMobile(), row.getRowNo());
        if (firstRowNo != null) {
            errors.add(error(row, "手机号在导入文件中重复，首次出现行号：" + firstRowNo));
        }
    }

    /**
     * 收集手机号。
     *
     * @param rows 导入行
     * @return 手机号列表
     */
    private List<String> collectMobiles(List<EmployeePerformanceImportItemCommand> rows) {
        List<String> mobiles = new ArrayList<>();
        for (EmployeePerformanceImportItemCommand row : rows) {
            if (row != null && StringUtils.isNotBlank(row.getMobile())) {
                mobiles.add(row.getMobile());
            }
        }
        return mobiles;
    }

    /**
     * 转换库内重复错误。
     *
     * @param rows 导入行
     * @param existingMobiles 已存在手机号
     * @return 错误明细
     */
    private List<EmployeePerformanceImportErrorResult> toExistingErrors(List<EmployeePerformanceImportItemCommand> rows,
                                                                        List<String> existingMobiles) {
        List<EmployeePerformanceImportErrorResult> errors = new ArrayList<>();
        if (existingMobiles == null || existingMobiles.isEmpty()) {
            return errors;
        }
        Set<String> existingMobileSet = new HashSet<>(existingMobiles);
        for (EmployeePerformanceImportItemCommand row : rows) {
            if (row != null && existingMobileSet.contains(row.getMobile())) {
                errors.add(error(row, "同一绩效任务下手机号已存在"));
            }
        }
        return errors;
    }

    /**
     * 转换绩效记录写入数据。
     *
     * @param command 员工绩效导入命令
     * @return 绩效记录写入数据
     */
    private List<EmployeePerformanceRecordData> toRecordData(EmployeePerformanceImportCommand command) {
        List<EmployeePerformanceRecordData> records = BeanUtil.copyToList(command.getRecords(), EmployeePerformanceRecordData.class);
        for (EmployeePerformanceRecordData record : records) {
            record.setTaskId(command.getTaskId());
            record.setConfirmStatus(PerformanceConfirmStatus.PENDING_CONFIRM.getCode());
            record.setFeedbackStatus(PerformanceFeedbackStatus.NONE.getCode());
        }
        return records;
    }

    /**
     * 构建导入成功结果。
     *
     * @param successCount 成功导入数量
     * @return 导入结果
     */
    private EmployeePerformanceImportResult successResult(int successCount) {
        EmployeePerformanceImportResult result = new EmployeePerformanceImportResult();
        result.setSuccess(Boolean.TRUE);
        result.setSuccessCount(successCount);
        result.setErrors(new ArrayList<>());
        return result;
    }

    /**
     * 构建导入失败结果。
     *
     * @param errors 错误明细
     * @return 导入结果
     */
    private EmployeePerformanceImportResult failedResult(List<EmployeePerformanceImportErrorResult> errors) {
        EmployeePerformanceImportResult result = new EmployeePerformanceImportResult();
        result.setSuccess(Boolean.FALSE);
        result.setSuccessCount(0);
        result.setErrors(errors);
        return result;
    }

    /**
     * 构建错误明细。
     *
     * @param row 导入行
     * @param message 错误原因
     * @return 错误明细
     */
    private EmployeePerformanceImportErrorResult error(EmployeePerformanceImportItemCommand row, String message) {
        EmployeePerformanceImportErrorResult error = new EmployeePerformanceImportErrorResult();
        error.setRowNo(row.getRowNo());
        error.setMobile(row.getMobile());
        error.setErrorMessage(message);
        return error;
    }
}
