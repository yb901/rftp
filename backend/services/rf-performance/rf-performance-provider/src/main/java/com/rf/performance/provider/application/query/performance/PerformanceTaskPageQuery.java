package com.rf.performance.provider.application.query.performance;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 绩效任务分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerformanceTaskPageQuery extends PageQuery {

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 任务状态编码。
     */
    private String status;
}
