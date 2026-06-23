package com.rf.performance.provider.common.exception;

import com.zy.common.core.bo.Result;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 员工绩效全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class PerformanceGlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一错误结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        log.warn("员工绩效业务异常，code={}, message={}", exception.getErrorCode().getCode(), exception.getMessage());
        return Result.error(exception.getErrorCode(), exception.getMessage());
    }

    /**
     * 处理请求体读取异常。
     *
     * @param exception 请求体读取异常
     * @return 统一错误结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("员工绩效请求体读取异常", exception);
        return Result.error(ErrorCode.E999001, "请求参数格式错误");
    }

    /**
     * 处理请求参数绑定异常。
     *
     * @param exception 参数绑定异常
     * @return 统一错误结果
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        String message = firstErrorMessage(exception.getAllErrors());
        log.warn("员工绩效参数绑定异常，message={}", message);
        return Result.error(ErrorCode.E999001, message);
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一错误结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = firstErrorMessage(exception.getBindingResult().getAllErrors());
        log.warn("员工绩效参数校验异常，message={}", message);
        return Result.error(ErrorCode.E999001, message);
    }

    /**
     * 处理未知异常。
     *
     * @param exception 未知异常
     * @return 统一错误结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("员工绩效系统异常", exception);
        return Result.error(ErrorCode.E999999);
    }

    /**
     * 获取第一条校验错误文案。
     *
     * @param errors 校验错误列表
     * @return 错误文案
     */
    private String firstErrorMessage(Iterable<ObjectError> errors) {
        if (errors == null) {
            return ErrorCode.E999001.getMessage();
        }
        for (ObjectError error : errors) {
            if (error != null && StringUtils.isNotBlank(error.getDefaultMessage())) {
                return error.getDefaultMessage();
            }
        }
        return ErrorCode.E999001.getMessage();
    }
}
