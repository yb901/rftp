package com.zy.common.core.idcodec.autoconfigure;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.common.core.idcodec.jackson.IdCodecJacksonModule;
import com.zy.common.core.idcodec.web.IdCodecWebMvcConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnClass({ObjectMapper.class, WebMvcConfigurer.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "zy.id-codec", name = "enabled", havingValue = "true")
public class IdCodecAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdCodecJacksonModule.class)
    public Module idCodecJacksonModule() {
        return new IdCodecJacksonModule();
    }

    @Bean
    @ConditionalOnMissingBean(IdCodecWebMvcConfigurer.class)
    public IdCodecWebMvcConfigurer idCodecWebMvcConfigurer(ObjectMapper objectMapper) {
        return new IdCodecWebMvcConfigurer(objectMapper);
    }
}
