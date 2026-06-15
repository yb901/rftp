package com.zy.rfpt.mng.provider.application.command.socialsecurity;

import lombok.Data;

import java.io.Serializable;

/**
 * 社保缴费任务重试命令。
 */
@Data
public class SocialSecurityPaymentTaskRetryCommand implements Serializable {

    /** 任务编号。 */
    private Long taskId;

    /** 操作人。 */
    private String operator;
}
