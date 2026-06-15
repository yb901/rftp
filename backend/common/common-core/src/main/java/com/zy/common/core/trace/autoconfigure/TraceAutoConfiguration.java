package com.zy.common.core.trace.autoconfigure;

import com.zy.common.core.trace.web.TraceWebFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Auto configuration for lightweight trace ID propagation.
 */
@AutoConfiguration
@ConditionalOnClass(Filter.class)
public class TraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "traceWebFilterRegistration")
    public FilterRegistrationBean<TraceWebFilter> traceWebFilterRegistration() {
        FilterRegistrationBean<TraceWebFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceWebFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceWebFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
