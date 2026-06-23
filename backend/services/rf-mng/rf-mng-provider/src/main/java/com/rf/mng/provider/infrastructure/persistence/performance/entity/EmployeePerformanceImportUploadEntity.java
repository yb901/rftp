package com.rf.mng.provider.infrastructure.persistence.performance.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工绩效导入上传记录实体。
 */
@Data
public class EmployeePerformanceImportUploadEntity implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    private Long id;
    /** 绩效任务ID。 */
    private Long taskId;
    /** 绩效任务名称快照。 */
    private String taskName;
    /** 原始文件名。 */
    private String fileName;
    /** 原始文件Content-Type。 */
    private String originalContentType;
    /** 原始文件 OSS 访问地址。 */
    private String originalFileUrl;
    /** 失败明细文件名。 */
    private String failureFileName;
    /** 失败明细文件 OSS 访问地址。 */
    private String failureFileUrl;
    /** 总条数。 */
    private Integer totalCount;
    /** 成功条数。 */
    private Integer successCount;
    /** 失败条数。 */
    private Integer failCount;
    /** 导入状态。 */
    private String status;
    /** 失败原因。 */
    private String errorMessage;
    /** 创建管理员ID。 */
    private Long createAdminId;
    /** 创建管理员名称。 */
    private String createAdminName;
    /** 创建时间。 */
    private LocalDateTime gmtCreate;
    /** 修改时间。 */
    private LocalDateTime gmtModified;
}
