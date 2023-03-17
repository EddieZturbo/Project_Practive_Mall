package com.eddie.mall_seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 @author EddieZhang
 @create 2023-03-17 12:08 PM
 */
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.199.181:6379");
//                .setConnectTimeout(timeout) // 未设置使用默认
//                .setDatabase(redisProperties.getDatabase()) // 未设置使用默认
//                .setPassword(redisProperties.getPassword()); // 未设置使用默认
        return Redisson.create(config);
    }
}
