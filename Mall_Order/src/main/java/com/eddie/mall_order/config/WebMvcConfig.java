package com.eddie.mall_order.config;

import com.eddie.mall_order.common.JacksonObjectMapper;
import com.eddie.mall_order.interceptor.UserOrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-02-27 1:59 PM
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    UserOrderInterceptor userOrderInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("detail");
        registry.addViewController("/list").setViewName("list");
        registry.addViewController("/confirm").setViewName("confirm");
        registry.addViewController("/pay").setViewName("pay");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userOrderInterceptor).addPathPatterns("/**");
    }


}
