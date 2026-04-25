package com.example.handmademarket.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.handmademarket.entity.Order;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    List<Order> findByBuyerIdOrderByCreateTimeDesc(Integer buyerId);

    List<Order> findByBuyerIdAndStatusOrderByCreateTimeDesc(Integer buyerId, Integer status);

    List<Order> findBySellerIdOrderByCreateTimeDesc(Integer sellerId);

    List<Order> findBySellerIdAndStatusOrderByCreateTimeDesc(Integer sellerId, Integer status);

    List<Order> findAllByOrderByCreateTimeDesc();

    /** 查询超时未支付的订单（状态=0 且创建时间早于指定时间） */
    @Query("SELECT o FROM Order o WHERE o.status = 0 AND o.createTime < :deadline")
    List<Order> findExpiredPendingOrders(@Param("deadline") LocalDateTime deadline);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") Integer sellerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId AND o.status = :status")
    long countBySellerIdAndStatus(@Param("sellerId") Integer sellerId, @Param("status") Integer status);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status >= 1")
    BigDecimal sumAmountBySellerId(@Param("sellerId") Integer sellerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyerId = :buyerId")
    long countByBuyerId(@Param("buyerId") Integer buyerId);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.buyerId = :buyerId AND o.status >= 1")
    BigDecimal sumAmountByBuyerId(@Param("buyerId") Integer buyerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyerId = :buyerId AND o.status = :status")
    long countByBuyerIdAndStatus(@Param("buyerId") Integer buyerId, @Param("status") Integer status);
}

