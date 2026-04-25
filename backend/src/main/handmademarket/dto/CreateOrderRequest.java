package com.example.handmademarket.dto;

import java.util.List;

public class CreateOrderRequest {

    private List<OrderItemDTO> items;
    private String deliveryAddress;
    private String payType;
    private String remark;

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public static class OrderItemDTO {
        private Integer goodsId;
        private Integer quantity;

        public Integer getGoodsId() { return goodsId; }
        public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}