package com.eddie.mall_seckill.controller;

import com.eddie.common.utils.R;
import com.eddie.mall_seckill.service.SecKillService;
import com.eddie.mall_seckill.to.SeckillSkuRedisTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    public R getCurrentSecKillSkus() {
        List<SeckillSkuRedisTo> currentSecKillSkus = secKillService.getCurrentSecKillSkus();
        return R.ok().setData(currentSecKillSkus);
    }

    /**
     * 根据skuId查询当前商品是否参加秒杀活动
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSecKillInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = secKillService.getSecKillInfoBySkuId(skuId);
        return R.ok().setData(to);
    }

    /**
     * 秒杀商品
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num, Model model) {
        try {
            String orderSn = secKillService.kill(killId, key, num);
            model.addAttribute("orderSn",orderSn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }
}
