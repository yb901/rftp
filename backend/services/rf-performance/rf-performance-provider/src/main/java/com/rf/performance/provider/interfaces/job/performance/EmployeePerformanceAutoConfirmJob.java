package com.rf.performance.provider.interfaces.job.performance;

import com.rf.performance.provider.application.manager.performance.h5.EmployeePerformanceH5Manager;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 员工绩效超期自动确认 XXL-JOB。
 */
@Slf4j
@Component
public class EmployeePerformanceAutoConfirmJob {

    /**
     * 员工绩效 H5 应用编排。
     */
    @Resource
    private EmployeePerformanceH5Manager employeePerformanceH5Manager;

    /**
     * 自动确认超期未操作的绩效记录。
     */
    @XxlJob("employeePerformanceAutoConfirmJob")
    public void execute() {
        try {
            int processed = employeePerformanceH5Manager.autoConfirmExpiredRecords(500);
            if (processed > 0) {
                log.info("员工绩效自动确认完成, processed={}", processed);
            }
        } catch (Exception ex) {
            log.warn("员工绩效自动确认任务异常", ex);
            throw ex;
        }
    }
}
