package com.rf.mng.provider.interfaces.performance.param.admin;

import lombok.Data;

/**
 * 管理端员工绩效反馈处理请求参数。
 */
@Data
public class EmployeePerformanceFeedbackHandleCtrlParam {

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
