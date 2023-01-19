package com.eddie.mall_ware.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *  //TODO Mybatis提供的公共字段自动填充接口 --元数据对象处理器
 *  自定义元数据处理器并实现MetaObjectHandler接口
 *  在需要自动填充的公共字段上加上@TableField()注解 并指定fill填充规则
 @author EddieZhang
 @create 2023-01-18 5:20 PM
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        log.info("公共字段属性(createTime&updateTime)自动填充");

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        log.info("公共字段属性(updateTime)自动填充");
    }
}
