package com.rf.performance.provider.infrastructure.persistence.performance.mapper;

import com.rf.performance.provider.infrastructure.persistence.performance.entity.EmployeePerformanceRecordEntity;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceAdminRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.admin.EmployeePerformanceFeedbackRecord;
import com.rf.performance.provider.application.query.performance.EmployeePerformancePageQuery;
import com.rf.performance.provider.infrastructure.persistence.performance.entity.admin.EmployeePerformanceAdjustLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工绩效记录 Mapper。
 */
@Mapper
public interface EmployeePerformanceRecordMapper {

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
    long count(@Param("query") EmployeePerformancePageQuery query);

    /**
     * 按条件分页查询员工绩效记录。
     *
     * @param query 查询条件
     * @param offset 偏移量
     * @param limit 数量
     * @return 员工绩效记录
     */
    List<EmployeePerformanceAdminRecord> page(@Param("query") EmployeePerformancePageQuery query,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);

    /**
     * 按员工绩效记录 ID 批量查询反馈。
     *
     * @param recordIds 员工绩效记录 ID
     * @return 员工绩效反馈记录
     */
    List<EmployeePerformanceFeedbackRecord> listFeedbackByRecordIds(@Param("recordIds") List<Long> recordIds);

    /**
     * 查询任务下已存在的手机号。
     *
     * @param taskId 绩效任务 ID
     * @param mobiles 手机号列表
     * @return 已存在手机号列表
     */
    List<String> listExistingMobiles(@Param("taskId") Long taskId, @Param("mobiles") List<String> mobiles);

    /**
     * 批量新增员工绩效记录。
     *
     * @param records 员工绩效记录实体列表
     * @return 影响行数
     */
    int batchInsert(@Param("records") List<EmployeePerformanceRecordEntity> records);

    /**
     * 更新绩效并进入二次确认。
     *
     * @param id 员工绩效记录 ID
     * @param performance 调整后绩效
     * @return 影响行数
     */
    int updatePerformanceForSecondConfirm(@Param("id") Long id, @Param("performance") String performance);

    /**
     * 更新反馈为已调整。
     *
     * @param recordId 员工绩效记录 ID
     * @param handleOpinion 处理意见
     * @param handleAdminId 处理管理员 ID
     * @param handleAdminName 处理管理员名称
     * @return 影响行数
     */
    int markFeedbackAdjusted(@Param("recordId") Long recordId,
                             @Param("handleOpinion") String handleOpinion,
                             @Param("handleAdminId") Long handleAdminId,
                             @Param("handleAdminName") String handleAdminName);

    /**
     * 新增调整留痕。
     *
     * @param entity 调整留痕实体
     * @return 影响行数
     */
    int insertAdjustLog(EmployeePerformanceAdjustLogEntity entity);
}
