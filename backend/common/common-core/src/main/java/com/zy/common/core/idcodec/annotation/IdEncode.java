package com.zy.common.core.idcodec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks response fields whose internal Long ID value should be encoded.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdEncode {

    /**
     * When true, null and values less than or equal to zero are serialized as null.
     */
    boolean gtZeroOnly() default false;
}
