package com.rf.mng.provider.application.manager.socialsecurity.impl;

import com.zy.common.core.bo.PageResp;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.rf.mng.provider.application.command.socialsecurity.SocialSecurityPaymentBatchCreateCommand;
import com.rf.mng.provider.application.command.socialsecurity.SocialSecurityPaymentTaskRetryCommand;
import com.rf.mng.provider.application.manager.socialsecurity.SocialSecurityPaymentManager;
import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentBatchPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityRobotReferencePersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.SocialSecurityPaymentTaskPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.SocialSecurityConfigPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.record.SocialSecurityEnterpriseRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentBatchData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.data.SocialSecurityPaymentTaskData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentBatchRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentTaskRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.record.SocialSecurityPaymentTaskSummaryRecord;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.rf.mng.provider.application.result.socialsecurity.SocialSecurityPaymentBatchResult;
import com.rf.mng.provider.application.result.socialsecurity.SocialSecurityPaymentTaskResult;
import com.rf.mng.provider.domain.socialsecurity.SocialSecurityPaymentBatchStatus;
import com.rf.mng.provider.domain.socialsecurity.SocialSecurityPaymentTaskStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 社保缴费配置持久化端口。 */
    @Resource
    private SocialSecurityConfigPersistencePort configPersistencePort;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManager")
    public Long createBatch(SocialSecurityPaymentBatchCreateCommand command) {
        validateCreateCommand(command);
        List<String> taxNos = resolveTaxNos(command);
        validateRunnableEnterprises(command, taxNos);
        SocialSecurityPaymentBatchData batch = new SocialSecurityPaymentBatchData();
        batch.setRegionCode(command.getRegionCode());
        batch.setSiteType(StringUtils.defaultIfBlank(command.getSiteType(), "default"));
        batch.setPeriodMonth(command.getPeriodMonth());
        batch.setStatus(SocialSecurityPaymentBatchStatus.SUBMITTED.name());
        batch.setTotalCount(taxNos.size());
        batch.setSuccessCount(0);
        batch.setFailedCount(0);
        batch.setCreateAdminId(command.getCreateAdminId());
        batch.setCreateAdminName(command.getCreateAdminName());
        batchPersistencePort.insert(batch);

        List<SocialSecurityPaymentTaskData> tasks = buildTasks(batch, command, taxNos);
        taskPersistencePort.batchInsert(tasks);
        return batch.getId();
    }

    @Override
    public PageResp<SocialSecurityPaymentBatchResult> pageBatch(SocialSecurityPaymentBatchQuery query) {
        long total = batchPersistencePort.count(query);
        List<SocialSecurityPaymentBatchRecord> batches = batchPersistencePort.page(query);
        fillRegionNames(batches);
        fillBatchTaskSummaries(batches);
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
    @Transactional(rollbackFor = Exception.class, transactionManager = "robotTransactionManager")
    public void retryTask(SocialSecurityPaymentTaskRetryCommand command) {
        SocialSecurityPaymentTaskRecord task = taskPersistencePort.findById(command.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.E999003, "任务不存在");
        }
        if (!Boolean.TRUE.equals(task.getRetryable())) {
            throw new BusinessException(ErrorCode.E999001, "当前任务不允许重试");
        }
        if (taskPersistencePort.markRetry(command.getTaskId()) != 1) {
            throw new BusinessException(ErrorCode.E999001, "重试次数已达上限");
        }
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
        if (regionCodes.isEmpty()) {
            return;
        }
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
     * 批量回填社保缴费批次任务汇总。
     *
     * @param batches 社保缴费批次
     */
    private void fillBatchTaskSummaries(List<SocialSecurityPaymentBatchRecord> batches) {
        if (batches == null || batches.isEmpty()) {
            return;
        }
        List<Long> batchIds = batches.stream()
                .map(SocialSecurityPaymentBatchRecord::getId)
                .distinct()
                .toList();
        List<SocialSecurityPaymentTaskSummaryRecord> summaries = taskPersistencePort.listSummaryByBatchIds(batchIds);
        Map<Long, SocialSecurityPaymentTaskSummaryRecord> summaryMap = new HashMap<>();
        for (SocialSecurityPaymentTaskSummaryRecord summary : summaries) {
            summaryMap.put(summary.getBatchId(), summary);
        }
        for (SocialSecurityPaymentBatchRecord batch : batches) {
            SocialSecurityPaymentTaskSummaryRecord summary = summaryMap.get(batch.getId());
            if (summary == null) {
                continue;
            }
            batch.setTotalCount(defaultInt(summary.getTotalCount()));
            batch.setSuccessCount(defaultInt(summary.getSuccessCount()));
            batch.setFailedCount(defaultInt(summary.getFailedCount()));
            batch.setStatus(calculateBatchStatus(summary));
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
        if (taxNos.isEmpty()) {
            return;
        }
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
                                                             SocialSecurityPaymentBatchCreateCommand command,
                                                             List<String> taxNoList) {
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
            task.setStatus(SocialSecurityPaymentTaskStatus.PENDING.name());
            task.setRetryable(Boolean.FALSE);
            task.setRetryCount(0);
            task.setMaxRetryCount(3);
            task.setCreateAdminId(command.getCreateAdminId());
            task.setCreateAdminName(command.getCreateAdminName());
            tasks.add(task);
        }
        if (tasks.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "未找到需要缴费的企业税号");
        }
        return tasks;
    }

    private List<String> resolveTaxNos(SocialSecurityPaymentBatchCreateCommand command) {
        if (command.getTaxNoList() != null && command.getTaxNoList().stream().anyMatch(StringUtils::isNotBlank)) {
            return command.getTaxNoList().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .toList();
        }
        return configPersistencePort.listActiveEnterpriseByRegion(command.getRegionCode()).stream()
                .map(SocialSecurityEnterpriseRecord::getTaxNo)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

    /**
     * 入队前校验企业和地区站点配置，避免无效任务由 worker 领取后才失败。
     */
    private void validateRunnableEnterprises(SocialSecurityPaymentBatchCreateCommand command, List<String> taxNos) {
        if (taxNos == null || taxNos.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "未找到需要缴费的企业税号");
        }
        String siteType = StringUtils.defaultIfBlank(command.getSiteType(), "default");
        var regionSite = configPersistencePort.findRegionSiteByCodeAndType(command.getRegionCode(), siteType);
        if (regionSite == null || !"active".equals(regionSite.getStatus())) {
            throw new BusinessException(ErrorCode.E999001, "地区站点不存在或未启用");
        }
        for (String taxNo : taxNos) {
            SocialSecurityEnterpriseRecord enterprise = configPersistencePort.findEnterpriseByTaxNo(taxNo);
            if (enterprise == null) {
                throw new BusinessException(ErrorCode.E999001, "企业不存在：" + taxNo);
            }
            if (!"active".equals(enterprise.getStatus())) {
                throw new BusinessException(ErrorCode.E999001, "企业未启用：" + taxNo);
            }
            if (!command.getRegionCode().equals(enterprise.getRegionCode())) {
                throw new BusinessException(ErrorCode.E999001, "企业地区与批次地区不一致：" + taxNo);
            }
            if (StringUtils.isBlank(enterprise.getSecurityAccountName())) {
                throw new BusinessException(ErrorCode.E999001, "企业未配置社保账户：" + taxNo);
            }
        }
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
        result.setWpmTotalAmount(entity.getWpmTotalAmount());
        result.setCompareStatus(entity.getCompareStatus());
        result.setPaymentStatus(entity.getPaymentStatus());
        result.setCertificateStatus(entity.getCertificateStatus());
        result.setBmsFeedbackStatus(entity.getBmsFeedbackStatus());
        result.setBmsFeedbackStage(entity.getBmsFeedbackStage());
        result.setBmsFeedbackErrorMessage(entity.getBmsFeedbackErrorMessage());
        result.setDiagnosticDir(entity.getDiagnosticDir());
        result.setErrorCode(entity.getErrorCode());
        result.setErrorMessage(entity.getErrorMessage());
        result.setRetryable(entity.getRetryable());
        result.setWorkerId(entity.getWorkerId());
        result.setClaimedAt(entity.getClaimedAt());
        result.setHeartbeatAt(entity.getHeartbeatAt());
        result.setFinishedAt(entity.getFinishedAt());
        result.setResultPayload(entity.getResultPayload());
        result.setCompareResultPayload(entity.getCompareResultPayload());
        result.setPaymentResultPayload(entity.getPaymentResultPayload());
        result.setCertificateResultPayload(entity.getCertificateResultPayload());
        result.setBmsFeedbackResultPayload(entity.getBmsFeedbackResultPayload());
        result.setGmtModified(entity.getGmtModified());
        return result;
    }

    /**
     * 根据任务汇总计算批次状态。
     *
     * @param summary 任务汇总
     * @return 批次状态
     */
    private String calculateBatchStatus(SocialSecurityPaymentTaskSummaryRecord summary) {
        int totalCount = defaultInt(summary.getTotalCount());
        if (totalCount == 0) {
            return SocialSecurityPaymentBatchStatus.SUBMITTED.name();
        }
        int successCount = defaultInt(summary.getSuccessCount());
        int failedCount = defaultInt(summary.getFailedCount());
        int canceledCount = defaultInt(summary.getCanceledCount());
        int processingCount = defaultInt(summary.getProcessingCount());
        int finishedCount = successCount + failedCount + canceledCount;
        if (successCount == totalCount) {
            return SocialSecurityPaymentBatchStatus.SUCCESS.name();
        }
        if (failedCount == totalCount) {
            return SocialSecurityPaymentBatchStatus.FAILED.name();
        }
        if (finishedCount == totalCount) {
            return SocialSecurityPaymentBatchStatus.PARTIAL_SUCCESS.name();
        }
        if (processingCount > 0 || finishedCount > 0) {
            return SocialSecurityPaymentBatchStatus.RUNNING.name();
        }
        return SocialSecurityPaymentBatchStatus.SUBMITTED.name();
    }

    /**
     * 空整数按零处理。
     *
     * @param value 整数值
     * @return 非空整数
     */
    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
