package com.rf.mng.provider.application.port.persistence.performance;

import com.rf.mng.provider.application.port.persistence.performance.data.EmployeePerformanceImportUploadData;
import com.rf.mng.provider.application.port.persistence.performance.record.EmployeePerformanceImportUploadRecord;

import java.util.List;

/**
 * 员工绩效导入上传记录持久化端口。
 */
public interface EmployeePerformanceImportUploadPersistencePort {

    /**
     * 新增上传记录。
     *
     * @param data 上传记录写入数据
     * @return 影响行数
     */
    int insert(EmployeePerformanceImportUploadData data);

    /**
     * 按任务查询最近上传记录。
     *
     * @param taskId 绩效任务ID，可为空
     * @param limit 查询数量
     * @return 上传记录列表
     */
    List<EmployeePerformanceImportUploadRecord> listRecent(Long taskId, int limit);

    /**
     * 按ID查询上传记录。
     *
     * @param id 上传记录ID
     * @return 上传记录
     */
    EmployeePerformanceImportUploadRecord getById(Long id);
}
