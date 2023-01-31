package com.eddie.mall_goods.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * TODO 自定义Spring Cache配置
 @author EddieZhang
 @create 2023-01-31 6:52 PM
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
/*
    @ConfigurationProperties(prefix = "spring.cache")
    public class CacheProperties {TODO 配置文件绑定配置类
*/
@Slf4j
public class MySpringCacheConfig {

    @Autowired
    private CacheProperties cacheProperties;

    @Bean
    public RedisCacheConfiguration customRedisCacheConfiguration(){
        //获取默认的cache配置 后续的定制修改覆盖掉默认配置即可
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        //指定key的序列化
        defaultCacheConfig = defaultCacheConfig.serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        //指定value的序列化
        defaultCacheConfig = defaultCacheConfig.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redis = cacheProperties.getRedis();
        //指定time-to-live: 配置缓存存活时间(过期时间) 等相关配置 从yaml配置文件中获取
        //如果yaml文件中没有配置就使用defaultCacheConfig默认的配置

        if (redis.getTimeToLive() != null) {//配置ddl缓存的存活时间
            defaultCacheConfig = defaultCacheConfig.entryTtl(redis.getTimeToLive());
        }
        if (redis.getKeyPrefix() != null) {//配置key的前缀 没有配置就使用缓存的cacheNames作为前缀
            defaultCacheConfig = defaultCacheConfig.prefixKeysWith(redis.getKeyPrefix());
        }
        if (!redis.isCacheNullValues()) {//TODO 是否缓存null值 防止缓存穿透
            defaultCacheConfig = defaultCacheConfig.disableCachingNullValues();
        }
        if (!redis.isUseKeyPrefix()) {//是否使用key前缀
            defaultCacheConfig = defaultCacheConfig.disableKeyPrefix();
        }
        return defaultCacheConfig;
    }

}
