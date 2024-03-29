package com.eddie.mall_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableRedisHttpSession//整合SpringSession同时指定redis作为持久化的启动注解
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.eddie.mall_search.feign"})
public class MallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSearchApplication.class, args);
    }

}
