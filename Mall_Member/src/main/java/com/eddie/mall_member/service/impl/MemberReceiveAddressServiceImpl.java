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

    /**
     * 根据地址id模拟计算出运费
     * @param addrId
     * @return
     */
    @Override
    public FareVo deliveryFare(Long addrId) {
        FareVo fareVo = new FareVo();
        MemberReceiveAddressEntity memberReceiveAddressEntity = memberReceiveAddressService.getById(addrId);
        if (null != memberReceiveAddressEntity) {
            String phone = memberReceiveAddressEntity.getPhone();
            String fare = phone.substring(phone.length() - 1, phone.length());//模拟计算运费（截取phone的最后一位用作运费）
            fareVo.setFare(new BigDecimal(fare));
            fareVo.setAddress(memberReceiveAddressEntity);
            return fareVo;
        }
        return null;
    }

}