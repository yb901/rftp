package com.rfpt.performance.provider.interfaces.controller.h5.vo.auth;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效 H5 登录返回对象。
 */
@Data
public class PerformanceH5LoginVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号。
     */
    private String mobile;
}
