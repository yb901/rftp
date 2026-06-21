package com.rfpt.performance.provider.application.command.performance.item;

import lombok.Data;

/**
 * 员工绩效导入明细命令。
 */
@Data
public class EmployeePerformanceImportItemCommand {

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
