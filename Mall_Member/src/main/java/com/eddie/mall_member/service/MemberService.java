package com.eddie.mall_member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eddie.common.utils.PageUtils;
import com.eddie.mall_member.entity.MemberEntity;
import com.eddie.mall_member.vo.MemberUserLoginVo;
import com.eddie.mall_member.vo.MemberUserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:05:50
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberUserRegisterVo vo);

    void checkUserNameUnique(String userName);

    void checkPhoneUnique(String phone);

    MemberEntity login(MemberUserLoginVo vo);
}

