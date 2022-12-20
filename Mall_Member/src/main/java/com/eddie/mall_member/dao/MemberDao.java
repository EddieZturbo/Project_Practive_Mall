package com.eddie.mall_member.dao;

import com.eddie.mall_member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:05:50
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
