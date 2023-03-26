package com.eddie.mall_goods.fallback;

import com.eddie.common.exception.BizCodeEnum;
import com.eddie.common.utils.R;
import com.eddie.mall_goods.feign.SecKillOpenFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 @author EddieZhang
 @create 2023-03-25 2:33 PM
 */
@Component
@Slf4j
public class SecKillOpenFeignFallback implements SecKillOpenFeign {
    @Override
    public R getSkuSecKillInfoBySkuId(Long skuId) {
        log.info("远程调用服务失败 服务降级回调");
        return R.error(BizCodeEnum.REMOTE_CALL_FAILED.getCode(),BizCodeEnum.REMOTE_CALL_FAILED.getMessage());
    }
}
