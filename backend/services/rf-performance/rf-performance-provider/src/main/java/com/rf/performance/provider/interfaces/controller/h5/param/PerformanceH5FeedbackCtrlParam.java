package com.rf.performance.provider.interfaces.controller.h5.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效 H5 反馈参数。
 */
@Data
public class PerformanceH5FeedbackCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 反馈内容。
     */
    private String feedbackContent;
}
