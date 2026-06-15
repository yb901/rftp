package com.zy.common.core.idcodec.web;

import com.zy.common.core.idcodec.annotation.IdDecode;
import com.zy.common.core.idcodec.support.IdCodecSupport;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IdDecodeArgumentResolver implements HandlerMethodArgumentResolver {

    private final IdCodecSupport support = new IdCodecSupport();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.hasParameterAnnotation(IdDecode.class)
                || support.shouldDecodeClass(parameter.getParameterType()))
                && !parameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Class<?> parameterType = parameter.getParameterType();
        if (support.isSimpleIdType(parameterType)) {
            return resolveSimpleValue(parameter, webRequest, binderFactory);
        }
        if (support.isArray(parameterType) || support.isCollection(parameterType)) {
            return resolveCollectionValue(parameter, webRequest);
        }
        Object instance = parameterType.getDeclaredConstructor().newInstance();
        for (Field field : support.getAllFields(parameterType)) {
            String[] values = webRequest.getParameterValues(field.getName());
            if (values != null) {
                resolveObjectField(parameter, webRequest, binderFactory, instance, field, values);
            }
        }
        validateIfNecessary(parameter, webRequest, binderFactory, instance);
        return instance;
    }

    private Object resolveSimpleValue(MethodParameter parameter,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) throws Exception {
        String parameterName = resolveParameterName(parameter);
        String value = webRequest.getParameter(parameterName);
        if (value == null) {
            value = getPathVariable(parameterName, webRequest);
        }
        Long decoded = support.decodeId(value, parameterName);
        WebDataBinder binder = binderFactory.createBinder(webRequest, null, parameterName);
        return binder.convertIfNecessary(decoded, parameter.getParameterType(), parameter);
    }

    private Object resolveCollectionValue(MethodParameter parameter, NativeWebRequest webRequest) {
        String parameterName = resolveParameterName(parameter);
        String[] values = webRequest.getParameterValues(parameterName);
        if (values == null) {
            return null;
        }
        List<Long> ids = support.decodeIdList(List.of(values), parameterName);
        if (support.isArray(parameter.getParameterType())) {
            return ids.toArray(new Long[0]);
        }
        if (support.isSet(parameter.getParameterType())) {
            return new HashSet<>(ids);
        }
        return ids;
    }

    private void resolveObjectField(MethodParameter parameter,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory,
                                    Object instance,
                                    Field field,
                                    String[] values) throws Exception {
        IdDecode annotation = field.getAnnotation(IdDecode.class);
        Object fieldValue;
        if (annotation != null && support.isSimpleIdType(field.getType())) {
            fieldValue = support.decodeId(values.length > 0 ? values[0] : null, field.getName());
        } else if (annotation != null && (support.isArray(field.getType()) || support.isCollection(field.getType()))) {
            List<Long> ids = support.decodeIdList(List.of(values), field.getName());
            if (support.isArray(field.getType())) {
                fieldValue = ids.toArray(new Long[0]);
            } else if (support.isSet(field.getType())) {
                fieldValue = new HashSet<>(ids);
            } else {
                fieldValue = ids;
            }
        } else {
            fieldValue = values.length > 0 ? values[0] : null;
        }
        setFieldValue(parameter, webRequest, binderFactory, instance, field, fieldValue);
    }

    private void setFieldValue(MethodParameter parameter,
                               NativeWebRequest webRequest,
                               WebDataBinderFactory binderFactory,
                               Object instance,
                               Field field,
                               Object fieldValue) throws Exception {
        field.setAccessible(true);
        WebDataBinder binder = binderFactory.createBinder(webRequest, null, field.getName());
        try {
            Object converted = binder.convertIfNecessary(fieldValue, field.getType(), field);
            field.set(instance, converted);
        } catch (ConversionNotSupportedException ex) {
            throw new MethodArgumentConversionNotSupportedException(fieldValue, ex.getRequiredType(),
                    field.getName(), parameter, ex.getCause());
        } catch (TypeMismatchException ex) {
            throw new MethodArgumentTypeMismatchException(fieldValue, ex.getRequiredType(),
                    field.getName(), parameter, ex.getCause());
        }
    }

    private String getPathVariable(String name, NativeWebRequest webRequest) {
        if (name == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> uriTemplateVariables = (Map<String, String>) webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
        if (uriTemplateVariables == null) {
            return null;
        }
        return uriTemplateVariables.get(name);
    }

    private String resolveParameterName(MethodParameter parameter) {
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            if (!pathVariable.value().isBlank()) {
                return pathVariable.value();
            }
            if (!pathVariable.name().isBlank()) {
                return pathVariable.name();
            }
        }
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            if (!requestParam.value().isBlank()) {
                return requestParam.value();
            }
            if (!requestParam.name().isBlank()) {
                return requestParam.name();
            }
        }
        return parameter.getParameterName();
    }

    private void validateIfNecessary(MethodParameter parameter,
                                     NativeWebRequest webRequest,
                                     WebDataBinderFactory binderFactory,
                                     Object instance) throws Exception {
        WebDataBinder binder = binderFactory.createBinder(webRequest, instance, parameter.getParameterName());
        if (binder.getTarget() == null) {
            return;
        }
        for (Annotation annotation : parameter.getParameterAnnotations()) {
            if (annotation instanceof Validated || annotation.annotationType().getSimpleName().startsWith("Valid")) {
                binder.validate();
                break;
            }
        }
        if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(parameter)) {
            throw new BindException(binder.getBindingResult());
        }
    }

    private boolean isBindExceptionRequired(MethodParameter parameter) {
        int index = parameter.getParameterIndex();
        Class<?>[] paramTypes = Objects.requireNonNull(parameter.getMethod()).getParameterTypes();
        boolean hasBindingResult = paramTypes.length > index + 1 && Errors.class.isAssignableFrom(paramTypes[index + 1]);
        return !hasBindingResult;
    }
}
