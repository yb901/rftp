package com.rf.performance.provider.application.port.persistence.performance;

import com.rf.performance.provider.application.port.persistence.performance.data.EmployeePerformanceRecordData;
import com.rf.performance.provider.application.port.persistence.performance.data.admin.EmployeePerformanceAdjustLogData;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceAdminRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceFeedbackRecord;
import com.rf.performance.provider.application.query.performance.EmployeePerformancePageQuery;

import java.util.List;

/**
 * 员工绩效记录持久化端口。
 */
public interface EmployeePerformanceRecordPersistencePort {

    /**
     * 按 ID 查询员工绩效记录。
     *
     * @param id 员工绩效记录 ID
     * @return 员工绩效记录
     */
    EmployeePerformanceAdminRecord getById(Long id);

    /**
     * 按条件统计员工绩效记录。
     *
     * @param query 查询条件
     * @return 总数
     */
    long count(EmployeePerformancePageQuery query);

    /**
     * 按条件分页查询员工绩效记录。
     *
     * @param query 查询条件
     * @return 员工绩效记录
     */
    List<EmployeePerformanceAdminRecord> page(EmployeePerformancePageQuery query);

    /**
     * 按员工绩效记录 ID 批量查询反馈。
     *
     * @param recordIds 员工绩效记录 ID
     * @return 员工绩效反馈记录
     */
    List<EmployeePerformanceFeedbackRecord> listFeedbackByRecordIds(List<Long> recordIds);

    /**
     * 查询任务下已存在的手机号。
     *
     * @param taskId 绩效任务 ID
     * @param mobiles 手机号列表
     * @return 已存在手机号列表
     */
    List<String> listExistingMobiles(Long taskId, List<String> mobiles);

    /**
     * 批量新增员工绩效记录。
     *
     * @param records 员工绩效记录写入数据
     * @return 影响行数
     */
    int batchInsert(List<EmployeePerformanceRecordData> records);

    /**
     * 更新绩效并进入二次确认。
     *
     * @param id 员工绩效记录 ID
     * @param performance 调整后绩效
     * @return 是否更新成功
     */
    boolean updatePerformanceForSecondConfirm(Long id, String performance);

    /**
     * 更新反馈为已调整。
     *
     * @param recordId 员工绩效记录 ID
     * @param handleOpinion 处理意见
     * @param handleAdminId 处理管理员 ID
     * @param handleAdminName 处理管理员名称
     * @return 是否更新成功
     */
    boolean markFeedbackAdjusted(Long recordId, String handleOpinion, Long handleAdminId, String handleAdminName);

    /**
     * 新增调整留痕。
     *
     * @param data 调整留痕写入数据
     * @return 是否新增成功
     */
    boolean insertAdjustLog(EmployeePerformanceAdjustLogData data);
}
