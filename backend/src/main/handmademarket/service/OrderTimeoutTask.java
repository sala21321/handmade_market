package com.example.handmademarket.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.handmademarket.entity.Order;
import com.example.handmademarket.repository.OrderRepository;

@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    /** 待支付订单超时时间（分钟） */
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    private final OrderRepository orderRepository;

    public OrderTimeoutTask(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 每分钟检查一次，自动取消超时未支付的订单
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(deadline);

        for (Order order : expiredOrders) {
            order.setStatus(5); // 已取消
            order.setCancelReason("订单已超时未支付，已自动取消");
            order.setPaymentStatus(0);
            orderRepository.save(order);
            log.info("订单 {} 超时未支付，已自动取消", order.getOrderId());
        }

        if (!expiredOrders.isEmpty()) {
            log.info("本次自动取消了 {} 个超时订单", expiredOrders.size());
        }
    }
}
