package com.eddie.mall_authserver.feign;

import com.eddie.common.utils.R;
import com.eddie.mall_authserver.vo.UserLoginVo;
import com.eddie.mall_authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 @author EddieZhang
 @create 2023-02-15 3:54 PM
 */
@FeignClient("cloud-mall-member")
public interface MemberOpenFeign {

    @PostMapping(value = "/mall_member/member/register")
    public R register(@RequestBody UserRegisterVo vo);

    @PostMapping(value = "/mall_member/member/login")
    public R login(@RequestBody UserLoginVo vo);
}
