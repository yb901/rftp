package com.rf.performance.api.query.performance;

import com.zy.common.core.bo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;

/**
 * 绩效任务分页查询 RPC 入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerformanceTaskPageParam extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 绩效描述。
     */
    private String performanceDescription;

    /**
     * 任务状态编码。
     */
    private String status;
}
