package com.eddie.mall_order.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 @author EddieZhang
 @create 2023-01-06 16:08
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Bean//TODO 配置redisTemplate 设置redis的key和value的序列化方式
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        //默认的Key序列化器为：JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key序列化
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

}
