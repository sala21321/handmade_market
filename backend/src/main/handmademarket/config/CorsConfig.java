package com.example.handmademarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 明确允许前端地址
                .allowedOrigins("http://localhost:3000")
                // 允许所有请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头（包括token、自定义头）
                .allowedHeaders("*")
                // 允许携带凭证（关键！解决token跨域问题）
                .allowCredentials(true)
                // 预检请求缓存时间
                .maxAge(3600);
    }
}