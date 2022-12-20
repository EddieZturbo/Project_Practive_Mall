package com.eddie.mall_member.controller;

import java.util.Arrays;
import java.util.Map;

import com.eddie.mall_member.openfeign.OpenFeignCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eddie.mall_member.entity.MemberEntity;
import com.eddie.mall_member.service.MemberService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;



/**
 * 会员
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:05:50
 */
@RefreshScope//nacons config 动态刷新开启
@RestController
@RequestMapping("mall_member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private OpenFeignCouponService openFeignCouponService;

    @Value("${member.username}")
    private String configUsername;
    @Value("${member.major}")
    private String configMajor;

    @RequestMapping("/configTest")
    public R configTest(){
        return R.ok("nacos config 配置中心管理并动态刷新").put("username",configUsername).put("major",configMajor);
    }



    @RequestMapping("/getMemberCoupon")
    public R getMemberCoupon(){
        return R.ok("这里是Eddie会员查询所有优惠卷").put("counts",openFeignCouponService.getCouponByMember().get("counts"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mall_member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
