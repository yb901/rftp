package com.rf.mng.provider.application.command.socialsecurity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 社保缴费任务重试命令。
 */
@Data
public class SocialSecurityPaymentTaskRetryCommand implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务编号。 */
    private Long taskId;
}
