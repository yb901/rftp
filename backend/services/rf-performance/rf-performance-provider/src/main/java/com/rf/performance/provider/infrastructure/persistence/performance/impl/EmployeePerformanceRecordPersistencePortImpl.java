package com.rf.performance.provider.infrastructure.persistence.performance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.rf.performance.provider.application.port.persistence.performance.EmployeePerformanceRecordPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.data.EmployeePerformanceRecordData;
import com.rf.performance.provider.application.port.persistence.performance.data.admin.EmployeePerformanceAdjustLogData;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceAdminRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceFeedbackRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceTaskStatRecord;
import com.rf.performance.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.EmployeePerformanceRecordEntity;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.admin.EmployeePerformanceAdjustLogEntity;
import com.rf.performance.provider.infrastructure.persistence.performance.mapper.EmployeePerformanceRecordMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 员工绩效记录持久化端口实现。
 */
@Repository
public class EmployeePerformanceRecordPersistencePortImpl implements EmployeePerformanceRecordPersistencePort {

    /**
     * 员工绩效记录 Mapper。
     */
    @Resource
    private EmployeePerformanceRecordMapper employeePerformanceRecordMapper;

    /**
     * 按 ID 查询员工绩效记录。
     *
     * @param id 员工绩效记录 ID
     * @return 员工绩效记录
     */
    @Override
    public EmployeePerformanceAdminRecord getById(Long id) {
        return employeePerformanceRecordMapper.getById(id);
    }

    /**
     * 按条件统计员工绩效记录。
     *
     * @param query 查询条件
     * @return 总数
     */
    @Override
    public long count(EmployeePerformancePageQuery query) {
        return employeePerformanceRecordMapper.count(query);
    }

    /**
     * 按条件分页查询员工绩效记录。
     *
     * @param query 查询条件
     * @return 员工绩效记录
     */
    @Override
    public List<EmployeePerformanceAdminRecord> page(EmployeePerformancePageQuery query) {
        int offset = Math.max(query.getPage() - 1, 0) * query.getSize();
        return employeePerformanceRecordMapper.page(query, offset, query.getSize());
    }

    /**
     * 按任务 ID 批量统计员工绩效记录。
     *
     * @param taskIds 绩效任务 ID
     * @return 员工绩效任务统计
     */
    @Override
    public List<EmployeePerformanceTaskStatRecord> listTaskStatsByTaskIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }
        return employeePerformanceRecordMapper.listTaskStatsByTaskIds(taskIds);
    }

    /**
     * 按员工绩效记录 ID 批量查询反馈。
     *
     * @param recordIds 员工绩效记录 ID
     * @return 员工绩效反馈记录
     */
    @Override
    public List<EmployeePerformanceFeedbackRecord> listFeedbackByRecordIds(List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return List.of();
        }
        return employeePerformanceRecordMapper.listFeedbackByRecordIds(recordIds);
    }

    /**
     * 查询任务下已存在的手机号。
     *
     * @param taskId 绩效任务 ID
     * @param mobiles 手机号列表
     * @return 已存在手机号列表
     */
    @Override
    public List<String> listExistingMobiles(Long taskId, List<String> mobiles) {
        return employeePerformanceRecordMapper.listExistingMobiles(taskId, mobiles);
    }

    /**
     * 批量新增员工绩效记录。
     *
     * @param records 员工绩效记录写入数据
     * @return 影响行数
     */
    @Override
    public int batchInsert(List<EmployeePerformanceRecordData> records) {
        List<EmployeePerformanceRecordEntity> entities = BeanUtil.copyToList(records, EmployeePerformanceRecordEntity.class);
        for (EmployeePerformanceRecordEntity entity : entities) {
            entity.setIsDeleted(0);
        }
        return employeePerformanceRecordMapper.batchInsert(entities);
    }

    /**
     * 更新绩效并进入二次确认。
     *
     * @param id 员工绩效记录 ID
     * @param performance 调整后绩效
     * @return 是否更新成功
     */
    @Override
    public boolean updatePerformanceForSecondConfirm(Long id, String performance) {
        return employeePerformanceRecordMapper.updatePerformanceForSecondConfirm(id, performance) > 0;
    }

    /**
     * 更新反馈为已调整。
     *
     * @param recordId 员工绩效记录 ID
     * @param handleOpinion 处理意见
     * @param handleAdminId 处理管理员 ID
     * @param handleAdminName 处理管理员名称
     * @return 是否更新成功
     */
    @Override
    public boolean markFeedbackAdjusted(Long recordId, String handleOpinion, Long handleAdminId, String handleAdminName) {
        return employeePerformanceRecordMapper.markFeedbackAdjusted(recordId, handleOpinion, handleAdminId, handleAdminName) > 0;
    }

    /**
     * 更新反馈为已处理未调整。
     *
     * @param recordId 员工绩效记录 ID
     * @param handleOpinion 处理意见
     * @param handleAdminId 处理管理员 ID
     * @param handleAdminName 处理管理员名称
     * @return 是否更新成功
     */
    @Override
    public boolean markFeedbackUnchanged(Long recordId, String handleOpinion, Long handleAdminId, String handleAdminName) {
        return employeePerformanceRecordMapper.markFeedbackUnchanged(recordId, handleOpinion, handleAdminId, handleAdminName) > 0;
    }

    /**
     * 更新绩效记录为反馈已处理未调整。
     *
     * @param id 员工绩效记录 ID
     * @return 是否更新成功
     */
    @Override
    public boolean markRecordFeedbackUnchanged(Long id) {
        return employeePerformanceRecordMapper.markRecordFeedbackUnchanged(id) > 0;
    }

    /**
     * 新增调整留痕。
     *
     * @param data 调整留痕写入数据
     * @return 是否新增成功
     */
    @Override
    public boolean insertAdjustLog(EmployeePerformanceAdjustLogData data) {
        EmployeePerformanceAdjustLogEntity entity = BeanUtil.copyProperties(data, EmployeePerformanceAdjustLogEntity.class);
        return employeePerformanceRecordMapper.insertAdjustLog(entity) > 0;
    }
}
