package com.rf.performance.provider.infrastructure.persistence.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.port.persistence.performance.EmployeePerformanceH5PersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.data.h5.PerformanceConfirmLogData;
import com.rf.performance.provider.application.port.persistence.performance.data.h5.PerformanceFeedbackData;
import com.rf.performance.provider.application.port.persistence.performance.data.h5.PerformanceSmsEvidenceData;
import com.rf.performance.provider.application.port.persistence.performance.record.h5.EmployeePerformanceH5Record;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.h5.PerformanceConfirmLogEntity;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.h5.PerformanceFeedbackEntity;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.h5.PerformanceSmsEvidenceEntity;
import com.rf.performance.provider.infrastructure.persistence.performance.mapper.EmployeePerformanceH5Mapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 员工绩效 H5 持久化端口实现。
 */
@Repository
public class EmployeePerformanceH5PersistencePortImpl implements EmployeePerformanceH5PersistencePort {

    /**
     * 员工绩效 H5 Mapper。
     */
    @Resource
    private EmployeePerformanceH5Mapper employeePerformanceH5Mapper;

    /**
     * 查询指定任务下首次确认待自动确认记录。
     *
     * @param taskIds 绩效任务 ID
     * @param limit 最大数量
     * @return 员工绩效记录
     */
    @Override
    public List<EmployeePerformanceH5Record> listPendingFirstConfirmRecordsByTaskIds(List<Long> taskIds, int limit) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }
        return employeePerformanceH5Mapper.listPendingFirstConfirmRecordsByTaskIds(taskIds, limit);
    }

    /**
     * 查询指定任务下二次确认待自动确认记录。
     *
     * @param taskIds 绩效任务 ID
     * @param limit 最大数量
     * @return 员工绩效记录
     */
    @Override
    public List<EmployeePerformanceH5Record> listPendingSecondConfirmRecordsByTaskIds(List<Long> taskIds, int limit) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }
        return employeePerformanceH5Mapper.listPendingSecondConfirmRecordsByTaskIds(taskIds, limit);
    }

    /**
     * 按手机号查询员工绩效记录。
     *
     * @param mobile 员工手机号
     * @return 员工绩效记录
     */
    @Override
    public List<EmployeePerformanceH5Record> listByMobile(String mobile) {
        return employeePerformanceH5Mapper.listByMobile(mobile);
    }

    /**
     * 按 ID 和手机号查询员工绩效记录。
     *
     * @param id 员工绩效记录 ID
     * @param mobile 员工手机号
     * @return 员工绩效记录
     */
    @Override
    public EmployeePerformanceH5Record getByIdAndMobile(Long id, String mobile) {
        return employeePerformanceH5Mapper.getByIdAndMobile(id, mobile);
    }

    /**
     * 新增短信验证留痕。
     *
     * @param data 短信验证留痕写入数据
     * @return 短信验证留痕 ID
     */
    @Override
    public Long insertSmsEvidence(PerformanceSmsEvidenceData data) {
        PerformanceSmsEvidenceEntity entity = BeanUtil.copyProperties(data, PerformanceSmsEvidenceEntity.class);
        employeePerformanceH5Mapper.insertSmsEvidence(entity);
        return entity.getId();
    }

    /**
     * 查询最新短信验证留痕。
     *
     * @param mobile 手机号
     * @param scene 短信场景
     * @return 短信验证留痕
     */
    @Override
    public PerformanceSmsEvidenceData getLatestSmsEvidence(String mobile, String scene) {
        PerformanceSmsEvidenceEntity entity = employeePerformanceH5Mapper.getLatestSmsEvidence(mobile, scene);
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, PerformanceSmsEvidenceData.class);
    }

    /**
     * 标记短信验证通过。
     *
     * @param data 短信验证留痕写入数据
     * @return 是否更新成功
     */
    @Override
    public boolean markSmsVerified(PerformanceSmsEvidenceData data) {
        PerformanceSmsEvidenceEntity entity = BeanUtil.copyProperties(data, PerformanceSmsEvidenceEntity.class);
        return employeePerformanceH5Mapper.markSmsVerified(entity) > 0;
    }

    /**
     * 新增确认留痕。
     *
     * @param data 确认留痕写入数据
     * @return 是否新增成功
     */
    @Override
    public boolean insertConfirmLog(PerformanceConfirmLogData data) {
        PerformanceConfirmLogEntity entity = BeanUtil.copyProperties(data, PerformanceConfirmLogEntity.class);
        return employeePerformanceH5Mapper.insertConfirmLog(entity) > 0;
    }

    /**
     * 批量新增确认留痕。
     *
     * @param dataList 确认留痕写入数据
     * @return 影响行数
     */
    @Override
    public int batchInsertConfirmLog(List<PerformanceConfirmLogData> dataList) {
        List<PerformanceConfirmLogEntity> entities = BeanUtil.copyToList(dataList, PerformanceConfirmLogEntity.class);
        return employeePerformanceH5Mapper.batchInsertConfirmLog(entities);
    }

    /**
     * 更新员工绩效记录为已确认。
     *
     * @param id 员工绩效记录 ID
     * @param mobile 员工手机号
     * @param confirmStatus 确认状态
     * @return 是否更新成功
     */
    @Override
    public boolean markConfirmed(Long id, String mobile, String confirmStatus) {
        return employeePerformanceH5Mapper.markConfirmed(id, mobile, confirmStatus) > 0;
    }

    /**
     * 批量自动确认。
     *
     * @param ids 员工绩效记录 ID
     * @param confirmStatus 确认状态
     * @return 影响行数
     */
    @Override
    public int batchMarkAutoConfirmed(List<Long> ids, String confirmStatus) {
        return employeePerformanceH5Mapper.batchMarkAutoConfirmed(ids, confirmStatus);
    }

    /**
     * 新增员工反馈。
     *
     * @param data 反馈写入数据
     * @return 是否新增成功
     */
    @Override
    public boolean insertFeedback(PerformanceFeedbackData data) {
        PerformanceFeedbackEntity entity = BeanUtil.copyProperties(data, PerformanceFeedbackEntity.class);
        entity.setIsDeleted(0);
        return employeePerformanceH5Mapper.insertFeedback(entity) > 0;
    }

    /**
     * 更新员工绩效记录为已反馈。
     *
     * @param id 员工绩效记录 ID
     * @param mobile 员工手机号
     * @return 是否更新成功
     */
    @Override
    public boolean markFeedbackSubmitted(Long id, String mobile) {
        return employeePerformanceH5Mapper.markFeedbackSubmitted(id, mobile) > 0;
    }
}
