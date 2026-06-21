package com.rfpt.performance.provider.application.port.persistence.performance;

import com.rfpt.performance.provider.application.port.persistence.performance.data.h5.PerformanceConfirmLogData;
import com.rfpt.performance.provider.application.port.persistence.performance.data.h5.PerformanceFeedbackData;
import com.rfpt.performance.provider.application.port.persistence.performance.data.h5.PerformanceSmsEvidenceData;
import com.rfpt.performance.provider.application.port.persistence.performance.record.h5.EmployeePerformanceH5Record;

import java.util.List;

/**
 * 员工绩效 H5 持久化端口。
 */
public interface EmployeePerformanceH5PersistencePort {

    /**
     * 查询指定任务下首次确认待自动确认记录。
     *
     * @param taskIds 绩效任务 ID
     * @param limit 最大数量
     * @return 员工绩效记录
     */
    List<EmployeePerformanceH5Record> listPendingFirstConfirmRecordsByTaskIds(List<Long> taskIds, int limit);

    /**
     * 查询指定任务下二次确认待自动确认记录。
     *
     * @param taskIds 绩效任务 ID
     * @param limit 最大数量
     * @return 员工绩效记录
     */
    List<EmployeePerformanceH5Record> listPendingSecondConfirmRecordsByTaskIds(List<Long> taskIds, int limit);

    /**
     * 按手机号查询员工绩效记录。
     *
     * @param mobile 员工手机号
     * @return 员工绩效记录
     */
    List<EmployeePerformanceH5Record> listByMobile(String mobile);

    /**
     * 按 ID 和手机号查询员工绩效记录。
     *
     * @param id 员工绩效记录 ID
     * @param mobile 员工手机号
     * @return 员工绩效记录
     */
    EmployeePerformanceH5Record getByIdAndMobile(Long id, String mobile);

    /**
     * 新增短信验证留痕。
     *
     * @param data 短信验证留痕写入数据
     * @return 短信验证留痕 ID
     */
    Long insertSmsEvidence(PerformanceSmsEvidenceData data);

    /**
     * 查询最新短信验证留痕。
     *
     * @param mobile 手机号
     * @param scene 短信场景
     * @return 短信验证留痕
     */
    PerformanceSmsEvidenceData getLatestSmsEvidence(String mobile, String scene);

    /**
     * 标记短信验证通过。
     *
     * @param data 短信验证留痕写入数据
     * @return 是否更新成功
     */
    boolean markSmsVerified(PerformanceSmsEvidenceData data);

    /**
     * 新增确认留痕。
     *
     * @param data 确认留痕写入数据
     * @return 是否新增成功
     */
    boolean insertConfirmLog(PerformanceConfirmLogData data);

    /**
     * 批量新增确认留痕。
     *
     * @param dataList 确认留痕写入数据
     * @return 影响行数
     */
    int batchInsertConfirmLog(List<PerformanceConfirmLogData> dataList);

    /**
     * 更新员工绩效记录为已确认。
     *
     * @param id 员工绩效记录 ID
     * @param mobile 员工手机号
     * @param confirmStatus 确认状态
     * @return 是否更新成功
     */
    boolean markConfirmed(Long id, String mobile, String confirmStatus);

    /**
     * 批量自动确认。
     *
     * @param ids 员工绩效记录 ID
     * @param confirmStatus 确认状态
     * @return 影响行数
     */
    int batchMarkAutoConfirmed(List<Long> ids, String confirmStatus);

    /**
     * 新增员工反馈。
     *
     * @param data 反馈写入数据
     * @return 是否新增成功
     */
    boolean insertFeedback(PerformanceFeedbackData data);

    /**
     * 更新员工绩效记录为已反馈。
     *
     * @param id 员工绩效记录 ID
     * @param mobile 员工手机号
     * @return 是否更新成功
     */
    boolean markFeedbackSubmitted(Long id, String mobile);
}
