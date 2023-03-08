package com.eddie.mall_goods;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.eddie.mall_goods.dao")
@EnableRedisHttpSession//整合SpringSession同时指定redis作为持久化的启动注解
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.eddie.mall_goods.feign"})
@EnableCaching//启动Spring Cache
@EnableTransactionManagement
public class MallGoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallGoodsApplication.class, args);
    }

}
