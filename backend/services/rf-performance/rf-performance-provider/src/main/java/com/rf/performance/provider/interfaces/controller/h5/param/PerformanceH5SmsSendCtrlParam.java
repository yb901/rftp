package com.rf.performance.provider.interfaces.controller.h5.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效 H5 短信发送参数。
 */
@Data
public class PerformanceH5SmsSendCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 短信场景。
     */
    private String scene;

    /**
     * 图形验证码凭证。
     */
    private String captchaTraceId;
}
