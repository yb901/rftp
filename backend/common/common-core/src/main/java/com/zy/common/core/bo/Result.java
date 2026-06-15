package com.zy.common.core.bo;

import com.zy.common.core.enums.ErrorCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.E000000.getCode());
        result.setMessage(ErrorCode.E000000.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        return result;
    }
}
