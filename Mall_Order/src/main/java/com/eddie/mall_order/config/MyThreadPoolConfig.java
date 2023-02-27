package com.eddie.mall_order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 @author EddieZhang
 @create 2023-02-08 9:16 PM
 */
@Configuration
public class MyThreadPoolConfig {

    /**
     * TODO 线程池配置(ThreadPoolExecutor)
     * @param threadPool
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties threadPool){
        return new ThreadPoolExecutor(
                threadPool.getCore(),
                threadPool.getMax(),
                threadPool.getLiveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
