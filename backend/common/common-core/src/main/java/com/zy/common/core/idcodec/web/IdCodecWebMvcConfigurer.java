package com.zy.common.core.idcodec.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

public class IdCodecWebMvcConfigurer implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    public IdCodecWebMvcConfigurer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new IdDecodeArgumentResolver());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        IdDecodeRequestBodyConverter converter = new IdDecodeRequestBodyConverter(objectMapper);
        int jacksonIndex = findJacksonConverterIndex(converters);
        if (jacksonIndex >= 0) {
            converters.add(jacksonIndex, converter);
        } else {
            converters.add(converter);
        }
    }

    private int findJacksonConverterIndex(List<HttpMessageConverter<?>> converters) {
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                return i;
            }
        }
        return -1;
    }
}
