package com.eddie.mall_coupon.dao;

import com.eddie.mall_coupon.entity.CouponSpuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 20:49:50
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {
	
}
