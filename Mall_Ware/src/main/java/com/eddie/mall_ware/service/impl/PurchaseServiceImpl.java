package com.eddie.mall_ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.common.constant.WareConstant;
import com.eddie.mall_ware.entity.PurchaseDetailEntity;
import com.eddie.mall_ware.service.PurchaseDetailService;
import com.eddie.mall_ware.service.WareSkuService;
import com.eddie.mall_ware.vo.MergeVo;
import com.eddie.mall_ware.vo.PurchaseDoneVo;
import com.eddie.mall_ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eddie.common.utils.PageUtils;
import com.eddie.common.utils.Query;

import com.eddie.mall_ware.dao.PurchaseDao;
import com.eddie.mall_ware.entity.PurchaseEntity;
import com.eddie.mall_ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceiveList(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> purchaseEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        purchaseEntityLambdaQueryWrapper//采购单的状态必须是1/0(未创建/未分配)
                .eq(PurchaseEntity::getStatus, 0)
                .or()
                .eq(PurchaseEntity::getStatus, 1);


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                purchaseEntityLambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        //判断是否指定了采购单 若采购单为null 则新建采购单并合并 反之合并进指定的采购单
        Long purchaseId = mergeVo.getPurchaseId();
        if (null == purchaseId) {//新建采购单并合并
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());//设置采购单的状态为新建
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();//获取新建的采购单的id(用于将采购需求合并至此采购单中)

        }
        //合并至采购单中
        List<Long> mergeVoItems = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        //TODO 判断是正确的状态才能进行merge
        PurchaseEntity purchaseEntity = this.getById(finalPurchaseId);
        if(WareConstant.PurchaseStatusEnum.CREATED.getCode() != purchaseEntity.getStatus() ||
                WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() != purchaseEntity.getStatus()){
            throw new RuntimeException("此采购单的状态目前以及无法进行合并");
        }
        List<PurchaseDetailEntity> purchaseDetailEntities = mergeVoItems.stream()
                .map(item -> {
                    PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();//修改采购需求实体类
                    purchaseDetailEntity.setId(item);
                    purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                    /*TODO lambda 表达式只能引用标记了 final 的外层局部变量，这就是说不能在 lambda 内部修改定义在域外的局部变量，否则会编译错误*/
                        /*在lambda表达式中对变量的操作都是基于原变量的副本，不会影响到原变量的值。
                        假定没有要求lambda表达式外部变量为final修饰，那么开发者会误以为外部变量的值能够在lambda表达式中被改变，而这实际是不可能的，
                        所以要求外部变量为final是在编译期以强制手段确保用户不会在lambda表达式中做修改原变量值的操作
                        lambda表达式是由匿名内部类演变过来的，它们的作用都是实现接口方法，于是类比匿名内部类，lambda表达式中使用的变量也需要是final类型
                        首先思考外部的局部变量finalI和匿名内部类里面的finalI是否是同一个变量？
                        我们知道，每个方法在执行的同时都会创建一个栈帧用于存储局部变量表、操作数栈、动态链接，方法出口等信息，
                        每个方法从调用直至执行完成的过程，就对应着一个栈帧在虚拟机栈中入栈到出栈的过程（《深入理解Java虚拟机》第2.2.2节 Java虚拟机栈）。
                        就是说在执行方法的时候，局部变量会保存在栈中，方法结束局部变量也会出栈，随后会被垃圾回收掉，
                        而此时，内部类对象可能还存在，如果内部类对象这时直接去访问局部变量的话就会出问题，
                        因为外部局部变量已经被回收了，解决办法就是把匿名内部类要访问的局部变量复制一份作为内部类对象的成员变量，
                        查阅资料或者通过反编译工具对代码进行反编译会发现，底层确实定义了一个新的变量，通过内部类构造函数将外部变量复制给内部类变量。
                        为何还需要用final修饰？
                        其实复制变量的方式会造一个数据不一致的问题，在执行方法的时候局部变量的值改变了却无法通知匿名内部类的变量，
                        随着程序的运行，就会导致程序运行的结果与预期不同，于是使用final修饰这个变量，使它成为一个常量，这样就保证了数据的一致性
                        */
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());//设置状态为已分配
                    return purchaseDetailEntity;
                })
                .collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);//进行批量update


    }

    @Override
    @Transactional
    public void receive(List<Long> ids) {
        //确定当前的采购单是新建或者已分配状态的
        List<PurchaseEntity> purchaseEntities = ids.stream()
                .map(id -> {
                    PurchaseEntity purchaseEntity = this.getById(id);
                    return purchaseEntity;
                })
                .filter(item -> {//使用filter进行状态的判断并进行过滤
                    if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                            item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                        return true;
                    }
                    return false;
                })
                .map(purchaseEntity -> {//将采购单的状态set为已领取
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    return purchaseEntity;//返回过滤以及处理好状态的采购单对象
                })
                .collect(Collectors.toList());

        //改变采购单的状态
        this.updateBatchById(purchaseEntities);

        //改变采购项的状态
        purchaseEntities.forEach(item -> {
            List<PurchaseDetailEntity> PurchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());

            List<PurchaseDetailEntity> UpdatePurchaseDetailEntities = PurchaseDetailEntities.stream()
                    .map(purchaseDetailEntity -> {
                        PurchaseDetailEntity purchaseDetailEntity1 = new PurchaseDetailEntity();
                        purchaseDetailEntity1.setId(purchaseDetailEntity.getId());
                        purchaseDetailEntity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());//修改为正在采购的状态
                        return purchaseDetailEntity1;
                    })
                    .collect(Collectors.toList());
            purchaseDetailService.updateBatchById(UpdatePurchaseDetailEntities);
        });

    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();//采购单id

        List<PurchaseDetailEntity> updates = new ArrayList<>();//创建一个List集合用来接收操作后的PurchaseDetailEntity
        //1)改变采购项的状态
        boolean flag = true;//记录采购的状态的标识
        List<PurchaseItemDoneVo> purchaseItemDoneVos = doneVo.getItems();//所有前端传来的采购项的信息
        for (PurchaseItemDoneVo p : purchaseItemDoneVos) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if(p.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;//若采购的状态为采购失败则将采购的状态标识置为false
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
            }else{
                //2)采购成功则进行入库操作
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());//设置采购的状态为已完成
                wareSkuService.addStock(purchaseDetailEntity.getSkuId(),purchaseDetailEntity.getWareId(),purchaseDetailEntity.getSkuNum());
            }
            purchaseDetailEntity.setId(p.getItemId());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);


        //3)改变采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        //根据采购的状态的标识flag来对采购单的status进行设置
        purchaseEntity.setStatus(flag?WareConstant.PurchaseDetailStatusEnum.FINISH.getCode():WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);



    }

}