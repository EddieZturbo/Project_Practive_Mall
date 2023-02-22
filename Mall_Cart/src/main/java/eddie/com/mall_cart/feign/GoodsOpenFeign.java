package eddie.com.mall_cart.feign;

import com.eddie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 @author EddieZhang
 @create 2023-02-22 3:20 PM
 */
@FeignClient("cloud-mall-goods")
public interface GoodsOpenFeign {
    /**
     * 远程调用商品服务根据skuId获取商品信息
     * @param skuId
     * @return
     */
    @RequestMapping("/mall_goods/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

    /**
     * 远程调用商品服务根据skuId获取到商品销售属性的指定信息的集合
     * @param skuId
     * @return
     */
    @GetMapping("/mall_goods/skusaleattrvalue/stringList/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);
}
