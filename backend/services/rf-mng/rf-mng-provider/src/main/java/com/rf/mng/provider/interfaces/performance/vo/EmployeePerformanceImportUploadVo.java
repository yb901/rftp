package com.rf.mng.provider.interfaces.performance.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工绩效导入上传记录 HTTP 返回对象。
 */
@Data
public class EmployeePerformanceImportUploadVo implements Serializable {

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
    /** 是否存在原始文件。 */
    private Boolean hasOriginalFile;
    /** 失败明细文件名。 */
    private String failureFileName;
    /** 是否存在失败明细文件。 */
    private Boolean hasFailureFile;
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
    /** 上传时间。 */
    private LocalDateTime gmtCreate;
}
