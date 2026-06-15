package com.zy.rfpt.mng.provider.application.manager.socialsecurity;

import com.zy.common.core.bo.PageResp;
import com.zy.rfpt.mng.provider.application.command.socialsecurity.SocialSecurityPaymentBatchCreateCommand;
import com.zy.rfpt.mng.provider.application.command.socialsecurity.SocialSecurityPaymentTaskRetryCommand;
import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.zy.rfpt.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.zy.rfpt.mng.provider.application.result.socialsecurity.SocialSecurityPaymentBatchResult;
import com.zy.rfpt.mng.provider.application.result.socialsecurity.SocialSecurityPaymentTaskResult;

/**
 * 社保缴费应用管理器。
 */
public interface SocialSecurityPaymentManager {

    /**
     * 创建并提交社保缴费批次。
     *
     * @param command 创建命令
     * @return 批次编号
     */
    Long createBatch(SocialSecurityPaymentBatchCreateCommand command);

    /**
     * 分页查询批次。
     *
     * @param query 查询条件
     * @return 批次分页
     */
    PageResp<SocialSecurityPaymentBatchResult> pageBatch(SocialSecurityPaymentBatchQuery query);

    /**
     * 分页查询任务。
     *
     * @param query 查询条件
     * @return 任务分页
     */
    PageResp<SocialSecurityPaymentTaskResult> pageTask(SocialSecurityPaymentTaskQuery query);

    /**
     * 重试失败任务。
     *
     * @param command 重试命令
     */
    void retryTask(SocialSecurityPaymentTaskRetryCommand command);
}
