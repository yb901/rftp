package com.zy.rfpt.mng.provider.interfaces.performance.vo.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效调整 HTTP 返回对象。
 */
@Data
public class EmployeePerformanceAdjustVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 员工绩效记录 ID。
     */
    private Long recordId;

    /**
     * 调整前绩效。
     */
    private String beforePerformance;

    /**
     * 调整后绩效。
     */
    private String afterPerformance;
}
