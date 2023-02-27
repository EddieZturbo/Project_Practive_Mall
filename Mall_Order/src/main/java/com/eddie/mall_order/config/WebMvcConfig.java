package com.eddie.mall_order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 @author EddieZhang
 @create 2023-02-27 1:59 PM
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("detail");
        registry.addViewController("/list").setViewName("list");
        registry.addViewController("/confirm").setViewName("confirm");
        registry.addViewController("/pay").setViewName("pay");
    }
}
