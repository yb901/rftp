package com.zy.rfpt.mng.provider.application.manager.socialsecurity.impl;

import com.zy.common.core.bo.PageResp;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.rfpt.mng.provider.application.command.socialsecurity.SocialSecurityPaymentBatchCreateCommand;
import com.zy.rfpt.mng.provider.application.command.socialsecurity.SocialSecurityPaymentTaskRetryCommand;
import com.zy.rfpt.mng.provider.application.manager.socialsecurity.SocialSecurityPaymentManager;
import com.zy.rfpt.mng.provider.application.port.gateway.robot.tax.TaxRobotGateway;
import com.zy.rfpt.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentBatchPersistencePort;
import com.zy.rfpt.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentTaskPersistencePort;
import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.zy.rfpt.mng.provider.application.result.socialsecurity.SocialSecurityPaymentBatchResult;
import com.zy.rfpt.mng.provider.application.result.socialsecurity.SocialSecurityPaymentTaskResult;
import com.zy.rfpt.mng.provider.domain.socialsecurity.SocialSecurityPaymentBatchStatus;
import com.zy.rfpt.mng.provider.domain.socialsecurity.SocialSecurityPaymentTaskStatus;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentBatchEntity;
import com.zy.rfpt.mng.provider.infrastructure.persistence.socialsecurity.entity.SocialSecurityPaymentTaskEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 社保缴费应用管理器实现。
 */
@Service
public class SocialSecurityPaymentManagerImpl implements SocialSecurityPaymentManager {

    /** 批次持久化端口。 */
    @Resource
    private SocialSecurityPaymentBatchPersistencePort batchPersistencePort;

    /** 任务持久化端口。 */
    @Resource
    private SocialSecurityPaymentTaskPersistencePort taskPersistencePort;

    /** 税务机器人网关。 */
    @Resource
    private TaxRobotGateway taxRobotGateway;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManager")
    public Long createBatch(SocialSecurityPaymentBatchCreateCommand command) {
        validateCreateCommand(command);
        SocialSecurityPaymentBatchEntity batch = new SocialSecurityPaymentBatchEntity();
        batch.setRegionCode(command.getRegionCode());
        batch.setSiteType(StringUtils.defaultIfBlank(command.getSiteType(), "default"));
        batch.setPeriodMonth(command.getPeriodMonth());
        batch.setStatus(SocialSecurityPaymentBatchStatus.SUBMITTED.name());
        batch.setTotalCount(countTaxNos(command));
        batch.setSuccessCount(0);
        batch.setFailedCount(0);
        batch.setCreatedBy(command.getOperator());
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdatedAt(batch.getCreatedAt());
        batchPersistencePort.insert(batch);

        List<SocialSecurityPaymentTaskEntity> tasks = buildTasks(batch, command);
        taskPersistencePort.batchInsert(tasks);
        triggerAfterCommit(tasks);
        return batch.getId();
    }

    @Override
    public PageResp<SocialSecurityPaymentBatchResult> pageBatch(SocialSecurityPaymentBatchQuery query) {
        long total = batchPersistencePort.count(query);
        List<SocialSecurityPaymentBatchResult> list = batchPersistencePort.page(query).stream()
                .map(this::toBatchResult)
                .toList();
        return PageResp.of(list, total, query.getPage(), query.getSize());
    }

    @Override
    public PageResp<SocialSecurityPaymentTaskResult> pageTask(SocialSecurityPaymentTaskQuery query) {
        long total = taskPersistencePort.count(query);
        List<SocialSecurityPaymentTaskResult> list = taskPersistencePort.page(query).stream()
                .map(this::toTaskResult)
                .toList();
        return PageResp.of(list, total, query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManager")
    public void retryTask(SocialSecurityPaymentTaskRetryCommand command) {
        SocialSecurityPaymentTaskEntity task = taskPersistencePort.findById(command.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.E999003, "任务不存在");
        }
        if (!Boolean.TRUE.equals(task.getRetryable())) {
            throw new BusinessException(ErrorCode.E999001, "当前任务不允许重试");
        }
        taskPersistencePort.markRetry(command.getTaskId(), command.getOperator());
        triggerAfterCommit(List.of(task));
    }

    private void triggerAfterCommit(List<SocialSecurityPaymentTaskEntity> tasks) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (SocialSecurityPaymentTaskEntity task : tasks) {
                    taxRobotGateway.triggerSocialSecurityPayment(task.getTaxNo(), task.getSiteType(), task.getPeriodMonth());
                }
            }
        });
    }

    private int countTaxNos(SocialSecurityPaymentBatchCreateCommand command) {
        if (command.getTaxNoList() == null) {
            return 0;
        }
        return (int) command.getTaxNoList().stream().filter(StringUtils::isNotBlank).count();
    }

    private void validateCreateCommand(SocialSecurityPaymentBatchCreateCommand command) {
        if (StringUtils.isBlank(command.getRegionCode())) {
            throw new BusinessException(ErrorCode.E999001, "地区不能为空");
        }
        if (StringUtils.isBlank(command.getPeriodMonth())) {
            throw new BusinessException(ErrorCode.E999001, "费款所属月份不能为空");
        }
    }

    private List<SocialSecurityPaymentTaskEntity> buildTasks(SocialSecurityPaymentBatchEntity batch,
                                                             SocialSecurityPaymentBatchCreateCommand command) {
        List<String> taxNoList = command.getTaxNoList() == null ? List.of() : command.getTaxNoList();
        List<SocialSecurityPaymentTaskEntity> tasks = new ArrayList<>();
        for (String taxNo : taxNoList) {
            if (StringUtils.isBlank(taxNo)) {
                continue;
            }
            SocialSecurityPaymentTaskEntity task = new SocialSecurityPaymentTaskEntity();
            task.setBatchId(batch.getId());
            task.setTaxNo(taxNo.trim());
            task.setRegionCode(batch.getRegionCode());
            task.setSiteType(batch.getSiteType());
            task.setPeriodMonth(batch.getPeriodMonth());
            task.setStatus(SocialSecurityPaymentTaskStatus.PROCESSING.name());
            task.setRetryable(Boolean.FALSE);
            task.setRetryCount(0);
            task.setMaxRetryCount(3);
            task.setCreatedBy(command.getOperator());
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(task.getCreatedAt());
            tasks.add(task);
        }
        if (tasks.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "第一版需要传入纳税人识别号列表");
        }
        return tasks;
    }

    private SocialSecurityPaymentBatchResult toBatchResult(SocialSecurityPaymentBatchEntity entity) {
        SocialSecurityPaymentBatchResult result = new SocialSecurityPaymentBatchResult();
        result.setId(entity.getId());
        result.setRegionCode(entity.getRegionCode());
        result.setRegionName(entity.getRegionName());
        result.setPeriodMonth(entity.getPeriodMonth());
        result.setStatus(entity.getStatus());
        result.setTotalCount(entity.getTotalCount());
        result.setSuccessCount(entity.getSuccessCount());
        result.setFailedCount(entity.getFailedCount());
        result.setCreatedAt(entity.getCreatedAt());
        return result;
    }

    private SocialSecurityPaymentTaskResult toTaskResult(SocialSecurityPaymentTaskEntity entity) {
        SocialSecurityPaymentTaskResult result = new SocialSecurityPaymentTaskResult();
        result.setId(entity.getId());
        result.setBatchId(entity.getBatchId());
        result.setTaxNo(entity.getTaxNo());
        result.setEnterpriseName(entity.getEnterpriseName());
        result.setSecurityAccountName(entity.getSecurityAccountName());
        result.setRegionCode(entity.getRegionCode());
        result.setPeriodMonth(entity.getPeriodMonth());
        result.setStatus(entity.getStatus());
        result.setPayableAmount(entity.getPayableAmount());
        result.setErrorCode(entity.getErrorCode());
        result.setErrorMessage(entity.getErrorMessage());
        result.setRetryable(entity.getRetryable());
        result.setUpdatedAt(entity.getUpdatedAt());
        return result;
    }
}
