package com.rfpt.performance.provider.interfaces.controller.h5.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效 H5 登录参数。
 */
@Data
public class PerformanceH5LoginCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 短信验证码。
     */
    private String smsCode;
}
