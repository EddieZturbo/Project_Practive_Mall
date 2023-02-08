package com.eddie.mall_goods.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 @author EddieZhang
 @create 2023-02-08 9:21 PM
 */
@ConfigurationProperties(prefix = "goods.thread.pool")//将ThreadPool与配置文件goods.thread.pool为前缀绑定
@Component//注入到容器中
@Data
public class ThreadPoolProperties {
    private Integer core;//核心线程数量
    private Integer max;//最大线程数
    private Long liveTime;//存活时间
}
