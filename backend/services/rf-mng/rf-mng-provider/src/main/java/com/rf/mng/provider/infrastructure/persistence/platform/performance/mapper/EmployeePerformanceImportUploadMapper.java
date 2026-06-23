package com.rf.mng.provider.infrastructure.persistence.platform.performance.mapper;

import com.rf.mng.provider.infrastructure.persistence.performance.entity.EmployeePerformanceImportUploadEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工绩效导入上传记录 Mapper。
 */
@Mapper
public interface EmployeePerformanceImportUploadMapper {

    /** 新增上传记录。 */
    int insert(EmployeePerformanceImportUploadEntity entity);

    /** 查询最近上传记录。 */
    List<EmployeePerformanceImportUploadEntity> listRecent(@Param("taskId") Long taskId, @Param("limit") int limit);

    /** 按ID查询完整上传记录。 */
    EmployeePerformanceImportUploadEntity getById(@Param("id") Long id);
}
