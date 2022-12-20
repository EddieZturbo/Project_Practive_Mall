package com.eddie.mall_member.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eddie.mall_member.entity.MemberReceiveAddressEntity;
import com.eddie.mall_member.service.MemberReceiveAddressService;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.R;



/**
 * 会员收货地址
 *
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-16 21:05:50
 */
@RestController
@RequestMapping("mall_member/memberreceiveaddress")
public class MemberReceiveAddressController {
    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mall_member:memberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mall_member:memberreceiveaddress:info")
    public R info(@PathVariable("id") Long id){
		MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);

        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mall_member:memberreceiveaddress:save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.save(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mall_member:memberreceiveaddress:update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.updateById(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mall_member:memberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids){
		memberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
