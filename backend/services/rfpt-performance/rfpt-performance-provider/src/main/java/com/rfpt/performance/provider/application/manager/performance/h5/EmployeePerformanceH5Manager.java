package com.rfpt.performance.provider.application.manager.performance.h5;

import com.rfpt.performance.provider.application.command.performance.h5.PerformanceH5ConfirmCommand;
import com.rfpt.performance.provider.application.command.performance.h5.PerformanceH5FeedbackCommand;
import com.rfpt.performance.provider.application.command.performance.h5.PerformanceH5LoginCommand;
import com.rfpt.performance.provider.application.command.performance.h5.PerformanceH5SmsSendCommand;
import com.rfpt.performance.provider.application.result.performance.h5.EmployeePerformanceH5Result;
import com.rfpt.performance.provider.application.result.performance.h5.PerformanceH5LoginResult;

import java.util.List;

/**
 * 员工绩效 H5 应用编排。
 */
public interface EmployeePerformanceH5Manager {

    /**
     * 发送短信验证码。
     *
     * @param command 短信发送命令
     * @return 短信验证留痕 ID
     */
    Long sendSmsCode(PerformanceH5SmsSendCommand command);

    /**
     * 手机号登录。
     *
     * @param command 登录命令
     * @return 登录结果
     */
    PerformanceH5LoginResult login(PerformanceH5LoginCommand command);

    /**
     * 查询当前员工绩效记录。
     *
     * @param mobile 登录手机号
     * @return 员工绩效记录
     */
    List<EmployeePerformanceH5Result> listMine(String mobile);

    /**
     * 确认绩效。
     *
     * @param command 确认命令
     */
    void confirm(PerformanceH5ConfirmCommand command);

    /**
     * 提交绩效反馈。
     *
     * @param command 反馈命令
     */
    void feedback(PerformanceH5FeedbackCommand command);

    /**
     * 自动确认超期绩效记录。
     *
     * @param limit 单次处理上限
     * @return 处理数量
     */
    int autoConfirmExpiredRecords(int limit);
}
