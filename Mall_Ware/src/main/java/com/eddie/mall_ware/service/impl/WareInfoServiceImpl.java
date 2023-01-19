package com.eddie.mall_ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_ware.dao.WareInfoDao;
import com.eddie.mall_ware.entity.WareInfoEntity;
import com.eddie.mall_ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> wareInfoEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)){
            wareInfoEntityLambdaQueryWrapper
                    .eq(WareInfoEntity::getId,key)
                    .or()
                    .like(WareInfoEntity::getName,key)
                    .or()
                    .like(WareInfoEntity::getAddress,key)
                    .or()
                    .like(WareInfoEntity::getAreacode,key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

}