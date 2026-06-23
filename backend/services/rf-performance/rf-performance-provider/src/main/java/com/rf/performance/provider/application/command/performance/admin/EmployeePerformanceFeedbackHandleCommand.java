package com.rf.performance.provider.application.command.performance.admin;

import lombok.Data;

/**
 * 员工绩效反馈处理命令。
 */
@Data
public class EmployeePerformanceFeedbackHandleCommand {

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 处理意见。
     */
    private String handleOpinion;

    /**
     * 操作管理员 ID。
     */
    private Long operatorAdminId;

    /**
     * 操作管理员名称。
     */
    private String operatorAdminName;
}
