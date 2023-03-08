package com.eddie.mall_order.listener;

import com.eddie.mall_order.entity.OrderEntity;
import com.eddie.mall_order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 @author EddieZhang
 @create 2023-03-08 8:49 PM
 */
@RabbitListener(queues = "order.release.order.queue")
@Component
public class OrderCloseListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void closeOrderListener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单" + orderEntity.getOrderSn());
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
