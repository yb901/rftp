package com.zy.common.core.idcodec.support;

import com.zy.common.core.idcodec.annotation.IdDecode;
import com.zy.common.utils.IdEncodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class IdCodecSupport {

    public Long decodeId(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Long id = IdEncodeUtil.decodeId(value);
        if (id == null) {
            log.warn("illegal encoded id, field={}, value={}", fieldName, value);
        }
        return id;
    }

    public String encodeId(Long value, boolean gtZeroOnly) {
        if (value == null) {
            return null;
        }
        if (gtZeroOnly && value <= 0) {
            return null;
        }
        return IdEncodeUtil.encodeId(value);
    }

    public List<Long> decodeIdList(Collection<?> values, String fieldName) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .flatMap(value -> splitCommaValues(value.toString()).stream())
                .map(value -> decodeId(value, fieldName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> splitCommaValues(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    public boolean isSimpleIdType(Class<?> type) {
        return type.isPrimitive()
                || Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type);
    }

    public boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    public boolean isArray(Class<?> type) {
        return type.isArray();
    }

    public boolean isMap(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    public boolean isSet(Class<?> type) {
        return Set.class.isAssignableFrom(type);
    }

    public List<Field> getAllFields(Class<?> type) {
        if (type == null || type.isInterface()) {
            return Collections.emptyList();
        }
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    public boolean shouldDecodeClass(Class<?> type) {
        return type != null && type.isAnnotationPresent(IdDecode.class);
    }
}
