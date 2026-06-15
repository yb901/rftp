package com.zy.common.core.idcodec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks controller parameters or fields whose external ID value should be decoded.
 */
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdDecode {

    /**
     * Required for collection and array fields when elements are nested objects.
     */
    Class<?> type() default Object.class;
}
