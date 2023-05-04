package com.eddie.mall_ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.to.OrderTo;
import com.eddie.common.to.SkuHasStockTo;
import com.eddie.common.to.mq.StockDetailTo;
import com.eddie.common.to.mq.StockLockedTo;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;
import com.eddie.common.utils.R;
import com.eddie.mall_ware.dao.WareSkuDao;
import com.eddie.mall_ware.entity.WareOrderTaskDetailEntity;
import com.eddie.mall_ware.entity.WareOrderTaskEntity;
import com.eddie.mall_ware.entity.WareSkuEntity;
import com.eddie.common.exception.NoStockException;
import com.eddie.mall_ware.feign.GoodsOpenFeign;
import com.eddie.mall_ware.feign.OrderOpenFeign;
import com.eddie.mall_ware.service.WareOrderTaskDetailService;
import com.eddie.mall_ware.service.WareOrderTaskService;
import com.eddie.mall_ware.service.WareSkuService;
import com.eddie.mall_ware.vo.OrderItemVo;
import com.eddie.mall_ware.vo.OrderVo;
import com.eddie.mall_ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    WareSkuService wareSkuService;

    @Autowired
    GoodsOpenFeign goodsOpenFeign;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderOpenFeign orderOpenFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> wareSkuEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            wareSkuEntityLambdaQueryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(skuId)) {
            wareSkuEntityLambdaQueryWrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> list = wareSkuService.list(new LambdaQueryWrapper<WareSkuEntity>().eq(WareSkuEntity::getSkuId, skuId));
        //1、判断如果还没有这个库存记录新增
        if (null == list || list.size() == 0) {
            //新增库存记录
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = goodsOpenFeign.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (0 == info.getCode()) {
                    skuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuService.save(skuEntity);//进行新增库存记录操作
        } else {
            //add库存
            wareSkuService.appendStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public void appendStock(Long skuId, Long wareId, Integer skuNum) {
        wareSkuDao.appendStock(skuId, wareId, skuNum);
    }

    /**
     * 根据商品的skuId查看是否有库存
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockTo> skuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> skuHasStockVos = skuIds.stream()
                .map(skuId -> {
                    SkuHasStockTo skuHasStockVo = new SkuHasStockTo();
                    //查询当前sku的总库存量
                    Long count = baseMapper.getSkuStock(skuId);
                    skuHasStockVo.setSkuId(skuId);
                    skuHasStockVo.setHasStock(count == null?false : count > 0L);//设置是否有库存
                    return skuHasStockVo;
                })
                .collect(Collectors.toList());
        return skuHasStockVos;
    }

    /**
     * 锁定库存方法
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单详情信息
         * 追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);
        //(理想化操作)按照下单的收货地址，找到一个就近仓库，锁定库存
        //找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();//获取需要上锁的商品的集合
        List<SkuWareHasStock> skuWareHasStockList = locks.stream().map((item) -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪个仓库有库存
            List<Long> wareIdList = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIdList);
            return stock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock hasStock : skuWareHasStockList) {
            boolean skuStocked = false;//每个sku商品是否成功锁定内存的状态标致
            Long skuId = hasStock.getSkuId();//需要上锁的skuID
            List<Long> wareIds = hasStock.getWareId();//对应skuId有货的仓库的id
            //判断wareIds是否为null 若为null表明没有任何仓库有货
            if(null == wareIds || wareIds.size() < 1){
                throw new NoStockException(skuId);//手动抛出没有库存的异常
            }

            //for循环遍历仓库对商品进行锁库存操作
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum());//调用wareSkuDao进行锁库存操作
                if(count == 1){
                    //商品库存锁定成功
                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("")
                            .skuNum(hasStock.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1)
                            .build();
                    //将库存工作单保存到指定的数据库中
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    //TODO 告诉MQ库存锁定成功
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    lockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);
                    break;//本次循环到的仓库锁定库存完成 跳出循环
                }else{
                    //当前仓库锁失败，重试下一个仓库
                }
            }

            //若上面的循环锁库操作一直未成功则表明当前商品所有仓库都没有锁住
            if (skuStocked == false) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
            //至此肯定全部都是锁定成功的
            return true;
        }
        


        return false;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        //库存工作单的id
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();
        /**
         * 解锁
         * 1、查询数据库关于这个订单锁定库存信息
         *   有：证明库存锁定成功了
         *      解锁：订单状况
         *          1、没有这个订单，必须解锁库存
         *          2、有这个订单，不一定解锁库存
         *              订单状态：已取消：解锁库存
         *                      已支付：不能解锁库存
         */
        WareOrderTaskDetailEntity taskDetailInfo = wareOrderTaskDetailService.getById(detailId);
        if(taskDetailInfo != null){
            //查出wms_ware_order_task工作单的信息
            Long id = to.getId();
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(id);
            //获取订单号查询订单状态
            String orderSn = orderTaskInfo.getOrderSn();
            //远程查询订单信息
            R orderData = orderOpenFeign.getOrderStatus(orderSn);
            if(orderData.getCode().equals(0)){
                //订单数据返回成功
                OrderVo orderInfo = orderData.getData("data", new TypeReference<OrderVo>() {});
                //判断订单状态是否已取消或者支付或者订单不存在
                if (orderInfo == null || orderInfo.getStatus() == 4) {
                    //订单已被取消，才能解锁库存
                    if (taskDetailInfo.getLockStatus() == 1) {
                        //当前库存工作单详情状态1，已锁定，但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }

            }else{
                //订单数据返回失败
                //消息拒绝以后重新放在队列里面，让别人继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        }else{
            //taskDetailInfo不存在
            //无需解锁
        }

    }

    /**
     * 解锁库存方法
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    private void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId) {
        //库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num);

        //更新工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        //变为已解锁
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);
    }

    /**
     * 订单关闭 进行库存解锁
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存优先到期，查订单状态新建，什么都不处理
     * 导致卡顿的订单，永远都不能解锁库存
     * @param orderTo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unlockStock(OrderTo orderTo) {

        String orderSn = orderTo.getOrderSn();
        //查一下最新的库存解锁状态，防止重复解锁库存
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        //按照工作单的id找到所有 没有解锁的库存，进行解锁
        Long id = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status", 1));

        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLockStock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }

    }


    /**
     * 内部类
     * 对应的skuID有库存的仓库id
     * 根据skuID查看有库存的id
     */
    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;//指定商品有货的仓库的id集合
    }

}