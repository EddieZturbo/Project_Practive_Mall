package com.eddie.mall_ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eddie.common.constant.WareConstant;
import com.eddie.mall_ware.entity.PurchaseDetailEntity;
import com.eddie.mall_ware.service.PurchaseDetailService;
import com.eddie.mall_ware.vo.MergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}