package com.eddie.mall_goods.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 @author EddieZhang
 @create 2023-01-13 21:03
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.eddie.mall_goods.controller")
public class MybatisConfig {
    /**
     * TODO 配置分页插件Interceptor
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //配置请求页面大于最大页面后操作 true调回到首页 默认false
        paginationInterceptor.setOverflow(true);
        //设置最大单页限制数量 默认500 -1表示不限制
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
