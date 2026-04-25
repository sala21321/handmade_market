package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_order")
public class Order {

    @Id
    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "buyer_id")
    private Integer buyerId;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "goods_id")
    private Integer goodsId;

    @Column(name = "custom_id")
    private Integer customId;

    @Column(name = "order_type")
    private Integer orderType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "deposit")
    private BigDecimal deposit;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "logistics_info", length = 200)
    private String logisticsInfo;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    @Column(name = "pay_type", length = 20)
    private String payType;

    @Column(name = "status")
    private Integer status;

    @Column(name = "payment_status")
    private Integer paymentStatus;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "dispute_status")
    private Integer disputeStatus;

    @Column(name = "dispute_result", length = 500)
    private String disputeResult;

    @Column(name = "dispute_time")
    private LocalDateTime disputeTime;

    @Column(name = "remark", length = 200)
    private String remark;

    // Getters and Setters

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getBuyerId() { return buyerId; }
    public void setBuyerId(Integer buyerId) { this.buyerId = buyerId; }

    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public Integer getCustomId() { return customId; }
    public void setCustomId(Integer customId) { this.customId = customId; }

    public Integer getOrderType() { return orderType; }
    public void setOrderType(Integer orderType) { this.orderType = orderType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getLogisticsInfo() { return logisticsInfo; }
    public void setLogisticsInfo(String logisticsInfo) { this.logisticsInfo = logisticsInfo; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }

    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }

    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public Integer getDisputeStatus() { return disputeStatus; }
    public void setDisputeStatus(Integer disputeStatus) { this.disputeStatus = disputeStatus; }

    public String getDisputeResult() { return disputeResult; }
    public void setDisputeResult(String disputeResult) { this.disputeResult = disputeResult; }

    public LocalDateTime getDisputeTime() { return disputeTime; }
    public void setDisputeTime(LocalDateTime disputeTime) { this.disputeTime = disputeTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}