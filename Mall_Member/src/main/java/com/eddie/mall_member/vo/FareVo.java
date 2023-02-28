package com.eddie.mall_member.vo;

import com.eddie.mall_member.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 @author EddieZhang
 @create 2023-02-28 11:28 PM
 */
@Data
public class FareVo {
    private MemberReceiveAddressEntity address;

    private BigDecimal fare;
}
