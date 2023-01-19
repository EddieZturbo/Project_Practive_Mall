package com.eddie.mall_ware.config;


import com.eddie.mall_ware.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-01-01 17:02
 */
@Slf4j
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    /**
     * //TODO 扩展mvc矿建中的消息转换器(对日期时间进行format指定格式化)
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展的消息转换器....");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器 底层使用Jackson 将Java对象转换成json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的消息转换器集合中
        converters.add(0,messageConverter);//追加到索引为0的位置 首选
    }
}
