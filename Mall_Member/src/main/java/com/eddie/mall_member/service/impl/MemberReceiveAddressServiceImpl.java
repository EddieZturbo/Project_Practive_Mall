package com.eddie.mall_member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.mall_member.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_member.dao.MemberReceiveAddressDao;
import com.eddie.mall_member.entity.MemberReceiveAddressEntity;
import com.eddie.mall_member.service.MemberReceiveAddressService;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据memberId查询member的地址列表
     * @param memberId
     * @return
     */
    @Override
    public List<MemberReceiveAddressEntity> getAddressByMemberId(Long memberId) {
        List<MemberReceiveAddressEntity> addressEntityList = this.baseMapper.selectList(
                new LambdaQueryWrapper<MemberReceiveAddressEntity>().eq(
                        MemberReceiveAddressEntity::getMemberId,
                        memberId));
        return addressEntityList;
    }


}