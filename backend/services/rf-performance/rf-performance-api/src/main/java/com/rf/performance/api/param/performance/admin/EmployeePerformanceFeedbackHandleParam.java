package com.rf.performance.api.param.performance.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效反馈处理 RPC 入参。
 */
@Data
public class EmployeePerformanceFeedbackHandleParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
