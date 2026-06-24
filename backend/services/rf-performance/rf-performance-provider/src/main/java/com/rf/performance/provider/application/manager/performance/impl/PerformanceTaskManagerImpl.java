package com.rf.performance.provider.application.manager.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.port.persistence.performance.EmployeePerformanceRecordPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.PerformanceTaskPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.data.PerformanceTaskData;
import com.rf.performance.provider.application.port.persistence.performance.record.PerformanceTaskRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceTaskStatRecord;
import com.rf.performance.provider.application.command.performance.PerformanceTaskCreateCommand;
import com.rf.performance.provider.application.manager.performance.PerformanceTaskManager;
import com.rf.performance.provider.application.query.performance.PerformanceTaskPageQuery;
import com.rf.performance.provider.application.result.performance.PerformanceTaskResult;
import com.rf.performance.provider.domain.performance.PerformanceTaskStatus;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 绩效任务应用编排实现。
 */
@Service
public class PerformanceTaskManagerImpl implements PerformanceTaskManager {

    /**
     * 默认二次确认宽限天数。
     */
    private static final long DEFAULT_SECOND_CONFIRM_DAYS = 3L;

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
     * 创建绩效任务。
     *
     * @param command 绩效任务创建命令
     * @return 绩效任务信息
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public PerformanceTaskResult createTask(PerformanceTaskCreateCommand command) {
        PerformanceTaskCreateCommand safeCommand = command == null ? new PerformanceTaskCreateCommand() : command;
        fillDefaultSecondConfirmDeadlineTime(safeCommand);
        validateCreateCommand(safeCommand);
        ensurePerformanceDescriptionUnique(safeCommand.getPerformanceDescription());
        PerformanceTaskRecord record = insertTask(safeCommand);
        PerformanceTaskResult result = BeanUtil.copyProperties(record, PerformanceTaskResult.class);
        result.setStatusCode(record.getStatus());
        return result;
    }

    /**
     * 补齐默认二次确认截止时间。
     *
     * @param command 绩效任务创建命令
     */
    private void fillDefaultSecondConfirmDeadlineTime(PerformanceTaskCreateCommand command) {
        if (command.getConfirmDeadlineTime() == null || command.getSecondConfirmDeadlineTime() != null) {
            return;
        }
        command.setSecondConfirmDeadlineTime(command.getConfirmDeadlineTime().plusDays(DEFAULT_SECOND_CONFIRM_DAYS));
    }

    /**
     * 分页查询绩效任务。
     *
     * @param query 绩效任务分页查询条件
     * @return 绩效任务分页
     */
    @Override
    public PageResp<PerformanceTaskResult> pageTasks(PerformanceTaskPageQuery query) {
        PerformanceTaskPageQuery safeQuery = query == null ? new PerformanceTaskPageQuery() : query;
        long total = performanceTaskPersistencePort.count(safeQuery);
        return PageResp.of(toResults(performanceTaskPersistencePort.page(safeQuery)), total, safeQuery.getPage(), safeQuery.getSize());
    }

    /**
     * 启用绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void enableTask(Long taskId) {
        updateTaskStatus(taskId, PerformanceTaskStatus.OPEN);
    }

    /**
     * 停用绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void disableTask(Long taskId) {
        updateTaskStatus(taskId, PerformanceTaskStatus.CLOSED);
    }

    /**
     * 删除绩效任务。
     *
     * @param taskId 绩效任务 ID
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        if (taskId == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务ID不能为空");
        }
        PerformanceTaskRecord taskRecord = performanceTaskPersistencePort.getById(taskId);
        if (taskRecord == null || Integer.valueOf(1).equals(taskRecord.getIsDeleted())) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务不存在");
        }
        if (!PerformanceTaskStatus.isClosed(taskRecord.getStatus())) {
            throw new BusinessException(ErrorCode.E999001, "仅关闭状态的绩效任务支持删除");
        }
        if (taskRecord.getTotalCount() != null && taskRecord.getTotalCount() > 0) {
            throw new BusinessException(ErrorCode.E999001, "已导入员工记录的绩效任务不支持删除");
        }
        boolean deleted = performanceTaskPersistencePort.deleteById(taskId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务删除失败");
        }
    }

    /**
     * 校验绩效任务创建命令。
     *
     * @param command 绩效任务创建命令
     */
    private void validateCreateCommand(PerformanceTaskCreateCommand command) {
        if (StringUtils.isBlank(command.getPerformanceDescription())) {
            throw new BusinessException(ErrorCode.E999001, "绩效描述不能为空");
        }
        command.setPerformanceDescription(command.getPerformanceDescription().trim());
        if (command.getPeriodStartDate() == null || command.getPeriodEndDate() == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效周期不能为空");
        }
        if (command.getPeriodEndDate().isBefore(command.getPeriodStartDate())) {
            throw new BusinessException(ErrorCode.E999001, "绩效周期结束日期不能早于开始日期");
        }
        if (command.getConfirmDeadlineTime() == null) {
            throw new BusinessException(ErrorCode.E999001, "首次确认截止时间不能为空");
        }
        if (command.getSecondConfirmDeadlineTime() != null
                && command.getSecondConfirmDeadlineTime().isBefore(command.getConfirmDeadlineTime())) {
            throw new BusinessException(ErrorCode.E999001, "二次确认截止时间不能早于首次确认截止时间");
        }
    }

    /**
     * 确保绩效描述未被任务占用。
     *
     * @param performanceDescription 绩效描述
     */
    private void ensurePerformanceDescriptionUnique(String performanceDescription) {
        boolean exists = performanceTaskPersistencePort.existsByPerformanceDescription(performanceDescription);
        if (exists) {
            throw new BusinessException(ErrorCode.E999001, "绩效描述已存在");
        }
    }

    /**
     * 新增绩效任务并转换唯一键冲突。
     *
     * @param command 绩效任务创建命令
     * @return 绩效任务读取记录
     */
    private PerformanceTaskRecord insertTask(PerformanceTaskCreateCommand command) {
        try {
            return performanceTaskPersistencePort.insert(toData(command));
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.E999001, "绩效描述已存在");
        }
    }

    /**
     * 转换绩效任务写入数据。
     *
     * @param command 绩效任务创建命令
     * @return 绩效任务写入数据
     */
    private PerformanceTaskData toData(PerformanceTaskCreateCommand command) {
        PerformanceTaskData data = BeanUtil.copyProperties(command, PerformanceTaskData.class);
        data.setStatus(PerformanceTaskStatus.OPEN.getCode());
        return data;
    }

    /**
     * 更新绩效任务状态。
     *
     * @param taskId 绩效任务 ID
     * @param status 目标状态
     */
    private void updateTaskStatus(Long taskId, PerformanceTaskStatus status) {
        PerformanceTaskRecord taskRecord = requireTask(taskId);
        if (status.getCode().equals(taskRecord.getStatus())) {
            return;
        }
        boolean updated = performanceTaskPersistencePort.updateStatus(taskId, status.getCode());
        if (!updated) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务状态更新失败");
        }
    }

    /**
     * 要求绩效任务存在。
     *
     * @param taskId 绩效任务 ID
     * @return 绩效任务记录
     */
    private PerformanceTaskRecord requireTask(Long taskId) {
        if (taskId == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务ID不能为空");
        }
        PerformanceTaskRecord taskRecord = performanceTaskPersistencePort.getById(taskId);
        if (taskRecord == null || Integer.valueOf(1).equals(taskRecord.getIsDeleted())) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务不存在");
        }
        return taskRecord;
    }

    /**
     * 转换绩效任务返回对象。
     *
     * @param records 绩效任务读取记录
     * @return 绩效任务返回对象
     */
    private java.util.List<PerformanceTaskResult> toResults(java.util.List<PerformanceTaskRecord> records) {
        java.util.List<PerformanceTaskResult> results = BeanUtil.copyToList(records, PerformanceTaskResult.class);
        for (PerformanceTaskResult result : results) {
            result.setStatusCode(records.stream()
                    .filter(record -> Objects.equals(record.getId(), result.getId()))
                    .findFirst()
                    .map(PerformanceTaskRecord::getStatus)
                    .orElse(null));
        }
        fillTaskStats(results);
        return results;
    }

    /**
     * 按员工记录实时回填任务统计。
     *
     * @param results 绩效任务返回对象
     */
    private void fillTaskStats(List<PerformanceTaskResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        List<Long> taskIds = results.stream()
                .map(PerformanceTaskResult::getId)
                .distinct()
                .toList();
        List<EmployeePerformanceTaskStatRecord> statRecords = employeePerformanceRecordPersistencePort.listTaskStatsByTaskIds(taskIds);
        Map<Long, EmployeePerformanceTaskStatRecord> statMap = new HashMap<>();
        for (EmployeePerformanceTaskStatRecord statRecord : statRecords) {
            statMap.put(statRecord.getTaskId(), statRecord);
        }
        for (PerformanceTaskResult result : results) {
            EmployeePerformanceTaskStatRecord statRecord = statMap.get(result.getId());
            if (statRecord == null) {
                result.setTotalCount(0);
                result.setConfirmedCount(0);
                result.setFeedbackCount(0);
                result.setAutoConfirmedCount(0);
                continue;
            }
            result.setTotalCount(statRecord.getTotalCount());
            result.setConfirmedCount(statRecord.getConfirmedCount());
            result.setFeedbackCount(statRecord.getFeedbackCount());
            result.setAutoConfirmedCount(statRecord.getAutoConfirmedCount());
        }
    }
}
