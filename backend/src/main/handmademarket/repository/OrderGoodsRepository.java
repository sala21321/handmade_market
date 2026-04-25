package com.example.handmademarket.repository;

import com.example.handmademarket.entity.OrderGoods;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderGoodsRepository extends JpaRepository<OrderGoods, Integer> {

    List<OrderGoods> findByOrderId(String orderId);
}
