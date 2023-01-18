package com.eddie.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

//TODO to对象是微服务间相互调用是进行传输的对象(微服务间的对象传输类似前后端的对象传输 都是以json格式传输并封装成指定的对象)
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
