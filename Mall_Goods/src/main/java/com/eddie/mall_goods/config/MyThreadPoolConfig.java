package com.eddie.mall_goods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

/**
 @author EddieZhang
 @create 2023-02-08 9:16 PM
 */
@Configuration
public class MyThreadPoolConfig {

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
