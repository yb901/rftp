package com.rf.mng.provider.application.command.performance;

import com.rf.mng.provider.application.command.performance.item.EmployeePerformanceImportItemCommand;
import lombok.Data;

import java.util.List;

/**
 * 管理端员工绩效导入命令。
 */
@Data
public class EmployeePerformanceImportCommand {

    /**
     * 绩效任务 ID。
     */
    private Long taskId;

    /**
     * 员工绩效明细列表。
     */
    private List<EmployeePerformanceImportItemCommand> records;
}
