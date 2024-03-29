package com.eddie.mall_member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@EnableRabbit//启用RabbitMQ
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.eddie.mall_member.openfeign")
@MapperScan("com.eddie.mall_member.dao")
public class MallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallMemberApplication.class, args);
    }

}
