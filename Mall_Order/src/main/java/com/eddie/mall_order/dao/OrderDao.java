package com.eddie.mall_order.dao;

import com.eddie.mall_order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:16:02
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
