package com.rf.mng.provider.application.manager.socialsecurity.impl;

import com.zy.common.core.bo.PageResp;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.rf.mng.provider.application.command.socialsecurity.SocialSecurityPaymentBatchCreateCommand;
import com.rf.mng.provider.application.command.socialsecurity.SocialSecurityPaymentTaskRetryCommand;
import com.rf.mng.provider.application.manager.socialsecurity.SocialSecurityPaymentManager;
import com.rf.mng.provider.application.port.gateway.robot.tax.TaxRobotGateway;
import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentBatchPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityRobotReferencePersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentTaskPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentBatchData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentTaskData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentBatchRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentTaskRecord;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.rf.mng.provider.application.result.socialsecurity.SocialSecurityPaymentBatchResult;
import com.rf.mng.provider.application.result.socialsecurity.SocialSecurityPaymentTaskResult;
import com.rf.mng.provider.domain.socialsecurity.SocialSecurityPaymentBatchStatus;
import com.rf.mng.provider.domain.socialsecurity.SocialSecurityPaymentTaskStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** 机器人基础信息持久化端口。 */
    @Resource
    private SocialSecurityRobotReferencePersistencePort robotReferencePersistencePort;

    /** 税务机器人网关。 */
    @Resource
    private TaxRobotGateway taxRobotGateway;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManager")
    public Long createBatch(SocialSecurityPaymentBatchCreateCommand command) {
        validateCreateCommand(command);
        SocialSecurityPaymentBatchData batch = new SocialSecurityPaymentBatchData();
        batch.setRegionCode(command.getRegionCode());
        batch.setSiteType(StringUtils.defaultIfBlank(command.getSiteType(), "default"));
        batch.setPeriodMonth(command.getPeriodMonth());
        batch.setStatus(SocialSecurityPaymentBatchStatus.SUBMITTED.name());
        batch.setTotalCount(countTaxNos(command));
        batch.setSuccessCount(0);
        batch.setFailedCount(0);
        batch.setCreateAdminId(command.getCreateAdminId());
        batch.setCreateAdminName(command.getCreateAdminName());
        batchPersistencePort.insert(batch);

        List<SocialSecurityPaymentTaskData> tasks = buildTasks(batch, command);
        taskPersistencePort.batchInsert(tasks);
        triggerAfterCommit(tasks);
        return batch.getId();
    }

    @Override
    public PageResp<SocialSecurityPaymentBatchResult> pageBatch(SocialSecurityPaymentBatchQuery query) {
        long total = batchPersistencePort.count(query);
        List<SocialSecurityPaymentBatchRecord> batches = batchPersistencePort.page(query);
        fillRegionNames(batches);
        List<SocialSecurityPaymentBatchResult> list = batches.stream()
                .map(this::toBatchResult)
                .toList();
        return PageResp.of(list, total, query.getPage(), query.getSize());
    }

    @Override
    public PageResp<SocialSecurityPaymentTaskResult> pageTask(SocialSecurityPaymentTaskQuery query) {
        long total = taskPersistencePort.count(query);
        List<SocialSecurityPaymentTaskRecord> tasks = taskPersistencePort.page(query);
        fillEnterpriseInfo(tasks);
        List<SocialSecurityPaymentTaskResult> list = tasks.stream()
                .map(this::toTaskResult)
                .toList();
        return PageResp.of(list, total, query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManager")
    public void retryTask(SocialSecurityPaymentTaskRetryCommand command) {
        SocialSecurityPaymentTaskRecord task = taskPersistencePort.findById(command.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.E999003, "任务不存在");
        }
        if (!Boolean.TRUE.equals(task.getRetryable())) {
            throw new BusinessException(ErrorCode.E999001, "当前任务不允许重试");
        }
        taskPersistencePort.markRetry(command.getTaskId());
        triggerAfterCommit(List.of(toTriggerTaskData(task)));
    }

    private void triggerAfterCommit(List<SocialSecurityPaymentTaskData> tasks) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (SocialSecurityPaymentTaskData task : tasks) {
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

    private SocialSecurityPaymentTaskData toTriggerTaskData(SocialSecurityPaymentTaskRecord record) {
        SocialSecurityPaymentTaskData data = new SocialSecurityPaymentTaskData();
        data.setTaxNo(record.getTaxNo());
        data.setSiteType(record.getSiteType());
        data.setPeriodMonth(record.getPeriodMonth());
        return data;
    }

    /**
     * 批量回填社保缴费批次地区名称。
     *
     * @param batches 社保缴费批次
     */
    private void fillRegionNames(List<SocialSecurityPaymentBatchRecord> batches) {
        if (batches == null || batches.isEmpty()) {
            return;
        }
        List<String> regionCodes = batches.stream()
                .map(SocialSecurityPaymentBatchRecord::getRegionCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        List<SocialSecurityPaymentBatchRecord> regions = robotReferencePersistencePort.listRegionNamesByRegionCodes(regionCodes);
        Map<String, String> regionNameMap = new HashMap<>();
        for (SocialSecurityPaymentBatchRecord region : regions) {
            regionNameMap.put(region.getRegionCode(), region.getRegionName());
        }
        for (SocialSecurityPaymentBatchRecord batch : batches) {
            batch.setRegionName(regionNameMap.get(batch.getRegionCode()));
        }
    }

    /**
     * 批量回填社保缴费任务企业信息。
     *
     * @param tasks 社保缴费任务
     */
    private void fillEnterpriseInfo(List<SocialSecurityPaymentTaskRecord> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<String> taxNos = tasks.stream()
                .map(SocialSecurityPaymentTaskRecord::getTaxNo)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        List<SocialSecurityPaymentTaskRecord> enterprises = robotReferencePersistencePort.listEnterpriseInfoByTaxNos(taxNos);
        Map<String, SocialSecurityPaymentTaskRecord> enterpriseMap = new HashMap<>();
        for (SocialSecurityPaymentTaskRecord enterprise : enterprises) {
            enterpriseMap.put(enterprise.getTaxNo(), enterprise);
        }
        for (SocialSecurityPaymentTaskRecord task : tasks) {
            SocialSecurityPaymentTaskRecord enterprise = enterpriseMap.get(task.getTaxNo());
            if (enterprise == null) {
                continue;
            }
            task.setEnterpriseName(enterprise.getEnterpriseName());
            task.setSecurityAccountName(enterprise.getSecurityAccountName());
        }
    }

    private void validateCreateCommand(SocialSecurityPaymentBatchCreateCommand command) {
        if (StringUtils.isBlank(command.getRegionCode())) {
            throw new BusinessException(ErrorCode.E999001, "地区不能为空");
        }
        if (StringUtils.isBlank(command.getPeriodMonth())) {
            throw new BusinessException(ErrorCode.E999001, "费款所属月份不能为空");
        }
    }

    private List<SocialSecurityPaymentTaskData> buildTasks(SocialSecurityPaymentBatchData batch,
                                                             SocialSecurityPaymentBatchCreateCommand command) {
        List<String> taxNoList = command.getTaxNoList() == null ? List.of() : command.getTaxNoList();
        List<SocialSecurityPaymentTaskData> tasks = new ArrayList<>();
        for (String taxNo : taxNoList) {
            if (StringUtils.isBlank(taxNo)) {
                continue;
            }
            SocialSecurityPaymentTaskData task = new SocialSecurityPaymentTaskData();
            task.setBatchId(batch.getId());
            task.setTaxNo(taxNo.trim());
            task.setRegionCode(batch.getRegionCode());
            task.setSiteType(batch.getSiteType());
            task.setPeriodMonth(batch.getPeriodMonth());
            task.setStatus(SocialSecurityPaymentTaskStatus.PROCESSING.name());
            task.setRetryable(Boolean.FALSE);
            task.setRetryCount(0);
            task.setMaxRetryCount(3);
            task.setCreateAdminId(command.getCreateAdminId());
            task.setCreateAdminName(command.getCreateAdminName());
            tasks.add(task);
        }
        if (tasks.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "第一版需要传入纳税人识别号列表");
        }
        return tasks;
    }

    private SocialSecurityPaymentBatchResult toBatchResult(SocialSecurityPaymentBatchRecord entity) {
        SocialSecurityPaymentBatchResult result = new SocialSecurityPaymentBatchResult();
        result.setId(entity.getId());
        result.setRegionCode(entity.getRegionCode());
        result.setRegionName(entity.getRegionName());
        result.setPeriodMonth(entity.getPeriodMonth());
        result.setStatus(entity.getStatus());
        result.setTotalCount(entity.getTotalCount());
        result.setSuccessCount(entity.getSuccessCount());
        result.setFailedCount(entity.getFailedCount());
        result.setGmtCreate(entity.getGmtCreate());
        return result;
    }

    private SocialSecurityPaymentTaskResult toTaskResult(SocialSecurityPaymentTaskRecord entity) {
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
        result.setGmtModified(entity.getGmtModified());
        return result;
    }
}
