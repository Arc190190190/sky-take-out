package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("处理支付超时订单:{}", LocalDateTime.now());
        // 查询支付超时订单
        List<Orders> timeOutOrders = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-1));
        if(timeOutOrders != null && timeOutOrders.size() > 0){
            //取消超时订单
            for (Orders orders : timeOutOrders) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理派送订单:{}", LocalDateTime.now());
        List<Orders> byStatusAndOrderTime = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if(byStatusAndOrderTime != null && byStatusAndOrderTime.size() > 0){
            for (Orders orders : byStatusAndOrderTime) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }


    }

}
