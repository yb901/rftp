package com.rf.mng.provider.common.config;

import com.rf.mng.provider.common.interceptor.MngAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 管理端 Web 配置。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 管理后台认证拦截器。 */
    @Resource
    private MngAuthInterceptor mngAuthInterceptor;

    /**
     * 配置跨域规则。
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册管理端认证拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mngAuthInterceptor)
                .addPathPatterns("/api/**", "/mng/**")
                .excludePathPatterns("/mng/auth/**", "/mng/health");
    }
}
