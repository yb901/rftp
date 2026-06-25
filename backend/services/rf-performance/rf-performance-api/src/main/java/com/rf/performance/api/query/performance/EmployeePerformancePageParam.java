package com.rf.performance.api.query.performance;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 员工绩效分页查询 RPC 入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmployeePerformancePageParam extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 确认状态编码集合。
     */
    private List<String> confirmStatusList;
}
