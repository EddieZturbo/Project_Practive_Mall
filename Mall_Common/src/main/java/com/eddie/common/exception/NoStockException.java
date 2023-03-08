package com.eddie.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 @author EddieZhang
 @create 2023-03-08 8:01 PM
 */
public class NoStockException extends RuntimeException{
    @Getter
    @Setter
    private Long skuId;
    public NoStockException(Long skuId) {
        super("商品id："+ skuId + "库存不足！");
    }

    public NoStockException(String msg) {
        super(msg);
    }
}
