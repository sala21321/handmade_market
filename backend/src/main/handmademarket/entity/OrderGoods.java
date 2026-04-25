package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_order_goods")
public class OrderGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "og_id")
    private Integer ogId;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "goods_id", nullable = false)
    private Integer goodsId;

    @Column(name = "goods_name", length = 100)
    private String goodsName;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "num")
    private Integer num;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Getters and Setters

    public Integer getOgId() { return ogId; }
    public void setOgId(Integer ogId) { this.ogId = ogId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getNum() { return num; }
    public void setNum(Integer num) { this.num = num; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}