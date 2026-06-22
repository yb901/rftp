package com.rf.mng.provider.interfaces.socialsecurity;

import com.zy.common.core.bo.PageResp;
import com.zy.common.core.bo.Result;
import com.rf.mng.provider.application.command.socialsecurity.SocialSecurityPaymentBatchCreateCommand;
import com.rf.mng.provider.application.command.socialsecurity.SocialSecurityPaymentTaskRetryCommand;
import com.rf.mng.provider.application.manager.socialsecurity.SocialSecurityPaymentManager;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentBatchQuery;
import com.rf.mng.provider.application.query.socialsecurity.SocialSecurityPaymentTaskQuery;
import com.rf.mng.provider.application.result.socialsecurity.SocialSecurityPaymentBatchResult;
import com.rf.mng.provider.application.result.socialsecurity.SocialSecurityPaymentTaskResult;
import com.rf.mng.provider.interfaces.socialsecurity.param.SocialSecurityPaymentBatchCreateCtrlParam;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 社保缴费管理接口。
 */
@RestController
@RequestMapping("/api/social-security-payments")
public class SocialSecurityPaymentController {

    /** 社保缴费应用管理器。 */
    @Resource
    private SocialSecurityPaymentManager socialSecurityPaymentManager;

    /** 创建社保缴费批次。 */
    @PostMapping("/batches")
    public Result<Long> createBatch(@RequestBody SocialSecurityPaymentBatchCreateCtrlParam param) {
        SocialSecurityPaymentBatchCreateCommand command = new SocialSecurityPaymentBatchCreateCommand();
        BeanUtils.copyProperties(param, command);
        return Result.success(socialSecurityPaymentManager.createBatch(command));
    }

    /** 分页查询社保缴费批次。 */
    @GetMapping("/batches")
    public Result<PageResp<SocialSecurityPaymentBatchResult>> pageBatch(SocialSecurityPaymentBatchQuery query) {
        return Result.success(socialSecurityPaymentManager.pageBatch(query));
    }

    /** 分页查询社保缴费任务。 */
    @GetMapping("/tasks")
    public Result<PageResp<SocialSecurityPaymentTaskResult>> pageTask(SocialSecurityPaymentTaskQuery query) {
        return Result.success(socialSecurityPaymentManager.pageTask(query));
    }

    /** 重试社保缴费任务。 */
    @PostMapping("/tasks/{taskId}/retry")
    public Result<Void> retryTask(@PathVariable Long taskId) {
        SocialSecurityPaymentTaskRetryCommand command = new SocialSecurityPaymentTaskRetryCommand();
        command.setTaskId(taskId);
        socialSecurityPaymentManager.retryTask(command);
        return Result.success();
    }
}
