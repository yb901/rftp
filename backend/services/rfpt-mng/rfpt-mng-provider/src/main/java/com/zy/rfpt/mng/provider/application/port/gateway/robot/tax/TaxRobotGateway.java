package com.zy.rfpt.mng.provider.application.port.gateway.robot.tax;

/**
 * 税务机器人网关。
 */
public interface TaxRobotGateway {

    /**
     * 触发社保缴费任务执行。
     *
     * @param taskId 任务编号
     */
    void triggerSocialSecurityPayment(Long taskId);
}
