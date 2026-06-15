package com.zy.common.core.oss;

import java.io.Serial;

/**
 * OSS operation runtime exception.
 */
public class AliOssException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AliOssException(String message) {
        super(message);
    }

    public AliOssException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AliOssException wrap(String message, Throwable cause) {
        if ("com.aliyun.oss.OSSException".equals(cause.getClass().getName())) {
            return new AliOssException(message + ": " + invokeStringMethod(cause, "getErrorCode")
                    + " - " + invokeStringMethod(cause, "getErrorMessage"), cause);
        }
        return new AliOssException(message, cause);
    }

    private static String invokeStringMethod(Throwable cause, String methodName) {
        try {
            Object value = cause.getClass().getMethod(methodName).invoke(cause);
            return value == null ? "" : value.toString();
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }
}
