package com.zy.rfpt.mng.provider.interfaces.performance.param.item;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工绩效导入明细 HTTP 参数。
 */
@Data
public class EmployeePerformanceImportItemCtrlParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 导入行号。
     */
    private Integer rowNo;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 员工手机号。
     */
    private String mobile;

    /**
     * 员工工号。
     */
    private String employeeNo;

    /**
     * 项目或部门。
     */
    private String projectDepartment;

    /**
     * 岗位。
     */
    private String positionName;

    /**
     * 绩效。
     */
    private String performance;
}
