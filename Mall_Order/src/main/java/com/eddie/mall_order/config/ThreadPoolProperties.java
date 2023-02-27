package com.eddie.mall_order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** TODO 线程池配置绑定外部Properties文件
 @author EddieZhang
 @create 2023-02-08 9:21 PM
 */
@ConfigurationProperties(prefix = "order.thread.pool")//将ThreadPool与配置文件goods.thread.pool为前缀绑定
@Component//注入到容器中
@Data
public class ThreadPoolProperties {
    private Integer core;//核心线程数量
    private Integer max;//最大线程数
    private Long liveTime;//存活时间
}
