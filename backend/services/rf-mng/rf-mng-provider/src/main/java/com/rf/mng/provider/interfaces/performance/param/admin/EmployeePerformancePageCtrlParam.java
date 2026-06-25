package com.rf.mng.provider.interfaces.performance.param.admin;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 员工绩效后台分页查询 HTTP 参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmployeePerformancePageCtrlParam extends PageQuery {

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 员工手机号。
     */
    private String mobile;

    /**
     * 确认状态编码。
     */
    private String confirmStatus;

    /**
     * 反馈状态编码。
     */
    private String feedbackStatus;

    /**
     * 导出确认范围，ALL/CONFIRMED/UNCONFIRMED。
     */
    private String exportConfirmScope;
}
