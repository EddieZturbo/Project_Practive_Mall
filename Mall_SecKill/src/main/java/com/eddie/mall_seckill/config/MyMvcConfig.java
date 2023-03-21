package com.eddie.mall_seckill.config;

import com.eddie.mall_seckill.inteceptor.SecKillInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 @author EddieZhang
 @create 2023-03-21 5:58 PM
 */
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {
    @Autowired
    SecKillInterceptor secKillInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(secKillInterceptor).addPathPatterns("/**");
    }
}
