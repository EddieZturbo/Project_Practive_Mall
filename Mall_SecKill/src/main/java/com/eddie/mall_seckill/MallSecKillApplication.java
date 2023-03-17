package com.eddie.mall_seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling//TODO 开启定时任务 TaskSchedulingAutoConfiguration
@EnableAsync//TODO 开启异步执行 TaskExecutionAutoConfiguration
public class MallSecKillApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallSecKillApplication.class, args);
    }
}
