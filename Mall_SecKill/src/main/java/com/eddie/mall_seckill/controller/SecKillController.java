package com.eddie.mall_seckill.controller;

import com.eddie.common.utils.R;
import com.eddie.mall_seckill.service.SecKillService;
import com.eddie.mall_seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-03-17 4:07 PM
 */
@Controller
public class SecKillController {
    @Autowired
    SecKillService secKillService;
    /**
     * 获取当前参与秒杀的商品信息
     * @return
     */
    @ResponseBody
    @GetMapping("/getCurrentSecKillSkus")
    public R getCurrentSecKillSkus(){
        List<SeckillSkuRedisTo> currentSecKillSkus = secKillService.getCurrentSecKillSkus();
        return R.ok().setData(currentSecKillSkus);
    }

}
