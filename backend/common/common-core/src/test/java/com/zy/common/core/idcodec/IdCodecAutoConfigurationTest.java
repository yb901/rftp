package com.zy.common.core.idcodec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.common.core.idcodec.autoconfigure.IdCodecAutoConfiguration;
import com.zy.common.core.idcodec.jackson.IdCodecJacksonModule;
import com.zy.common.core.idcodec.web.IdCodecWebMvcConfigurer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class IdCodecAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdCodecAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void shouldNotEnableWhenPropertyIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(IdCodecJacksonModule.class);
            assertThat(context).doesNotHaveBean(IdCodecWebMvcConfigurer.class);
        });
    }

    @Test
    void shouldEnableWhenPropertyIsTrue() {
        contextRunner
                .withPropertyValues("zy.id-codec.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(IdCodecJacksonModule.class);
                    assertThat(context).hasSingleBean(IdCodecWebMvcConfigurer.class);
                });
    }
}
