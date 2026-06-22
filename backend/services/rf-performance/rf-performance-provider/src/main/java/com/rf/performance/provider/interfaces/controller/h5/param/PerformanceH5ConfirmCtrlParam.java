package com.rf.performance.provider.interfaces.controller.h5.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效 H5 确认参数。
 */
@Data
public class PerformanceH5ConfirmCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短信验证码。
     */
    private String smsCode;
}
