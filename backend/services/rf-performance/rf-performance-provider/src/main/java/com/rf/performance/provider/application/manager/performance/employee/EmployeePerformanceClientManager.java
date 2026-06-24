package com.rf.performance.provider.application.manager.performance.employee;

import com.rf.performance.provider.application.command.performance.employee.EmployeePerformanceConfirmCommand;
import com.rf.performance.provider.application.command.performance.employee.EmployeePerformanceFeedbackCommand;
import com.rf.performance.provider.application.command.performance.employee.EmployeePerformanceLoginCommand;
import com.rf.performance.provider.application.command.performance.employee.EmployeePerformanceSmsSendCommand;
import com.rf.performance.provider.application.result.performance.employee.EmployeePerformanceClientResult;
import com.rf.performance.provider.application.result.performance.employee.EmployeePerformanceLoginResult;

import java.util.List;

/**
 * 员工端绩效应用编排。
 */
public interface EmployeePerformanceClientManager {

    /**
     * 发送短信验证码。
     *
     * @param command 短信发送命令
     * @return 短信验证留痕 ID
     */
    Long sendSmsCode(EmployeePerformanceSmsSendCommand command);

    /**
     * 手机号登录。
     *
     * @param command 登录命令
     * @return 登录结果
     */
    EmployeePerformanceLoginResult login(EmployeePerformanceLoginCommand command);

    /**
     * 判断手机号是否有当前待处理绩效。
     *
     * @param mobile 员工手机号
     * @return 是否有当前待处理绩效
     */
    boolean hasPendingPerformance(String mobile);

    /**
     * 查询员工绩效记录。
     *
     * @param mobile 登录手机号
     * @param includeHistory 是否包含历史记录
     * @return 员工绩效记录
     */
    List<EmployeePerformanceClientResult> listMine(String mobile, boolean includeHistory);

    /**
     * 确认绩效。
     *
     * @param command 确认命令
     */
    void confirm(EmployeePerformanceConfirmCommand command);

    /**
     * 提交绩效反馈。
     *
     * @param command 反馈命令
     */
    void feedback(EmployeePerformanceFeedbackCommand command);

    /**
     * 自动确认超期绩效记录。
     *
     * @param limit 单次处理上限
     * @return 处理数量
     */
    int autoConfirmExpiredRecords(int limit);
}
