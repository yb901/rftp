package com.zy.common.core.exception;

import com.zy.common.core.enums.ErrorCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
