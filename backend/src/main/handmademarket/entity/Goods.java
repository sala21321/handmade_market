package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "tb_goods")
public class Goods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goods_id")
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "goods_name")
    private String goodsName;

    @Column(name = "price")
    private Double price;

    @Column(name = "reserve_price")
    private Double reservePrice;

    @Column(name = "material")
    private String material;

    @Column(name = "size")
    private String size;

    @Column(name = "style")
    private String style;

    @Column(name = "delivery_cycle")
    private Integer deliveryCycle;

    @Column(name = "details")
    private String details;

    @Column(name = "images")
    private String images;

    @Column(name = "category")
    private String category;

    @Transient
    private String title;

    @Transient
    private String imageUrl;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "auditor_id")
    private Integer auditorId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "audit_remark")
    private String auditRemark;

    @Transient
    private Integer sortWeight; // 用于相关性排序的临时字段

    @PrePersist
    protected void onCreate() {
        publishTime = LocalDateTime.now();
    }

    // ==================== 全字段原生 Getter & Setter 方法 ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getReservePrice() {
        return reservePrice;
    }

    public void setReservePrice(Double reservePrice) {
        this.reservePrice = reservePrice;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Integer getDeliveryCycle() {
        return deliveryCycle;
    }

    public void setDeliveryCycle(Integer deliveryCycle) {
        this.deliveryCycle = deliveryCycle;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title != null ? title : goodsName;
    }

    public void setTitle(String title) {
        this.title = title;
        if (this.goodsName == null || this.goodsName.isBlank()) {
            this.goodsName = title;
        }
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : images;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if (this.images == null || this.images.isBlank()) {
            this.images = imageUrl;
        }
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public Integer getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Integer auditorId) {
        this.auditorId = auditorId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public Integer getSortWeight() {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight) {
        this.sortWeight = sortWeight;
    }
}