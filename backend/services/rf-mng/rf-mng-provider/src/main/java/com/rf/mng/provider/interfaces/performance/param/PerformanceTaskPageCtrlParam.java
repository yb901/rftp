package com.rf.mng.provider.interfaces.performance.param;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 绩效任务后台分页查询 HTTP 参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerformanceTaskPageCtrlParam extends PageQuery {

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 任务状态编码。
     */
    private String status;
}
