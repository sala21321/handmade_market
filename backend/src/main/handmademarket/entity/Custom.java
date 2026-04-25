package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_custom")
public class Custom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_id")
    private Integer customId;

    @Column(name = "consumer_id", nullable = false)
    private Integer consumerId;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "custom_desc", nullable = false, length = 500)
    private String customDesc;

    @Column(name = "reference_images", columnDefinition = "TEXT")
    private String referenceImages;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_wholesale")
    private Boolean isWholesale;

    @Column(name = "budget", nullable = false)
    private BigDecimal budget;

    @Column(name = "final_unit_price")
    private BigDecimal finalUnitPrice;

    @Column(name = "final_total_price", insertable = false, updatable = false)
    private BigDecimal finalTotalPrice;

    @Column(name = "cycle", nullable = false)
    private Integer cycle;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "style", length = 50)
    private String style;

    @Column(name = "contact", length = 50)
    private String contact;

    @Column(name = "deliver_content", columnDefinition = "TEXT")
    private String deliverContent;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "accept_time")
    private LocalDateTime acceptTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @Column(name = "match_creators", columnDefinition = "TEXT")
    private String matchCreators;

    @Column(name = "status")
    private Integer status; // 0-待匹配 1-沟通中 2-已接单 3-已完成 4-已取消 5-已拒绝

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    // Getters and Setters
    public Integer getCustomId() { return customId; }
    public void setCustomId(Integer customId) { this.customId = customId; }

    public Integer getConsumerId() { return consumerId; }
    public void setConsumerId(Integer consumerId) { this.consumerId = consumerId; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public String getCustomDesc() { return customDesc; }
    public void setCustomDesc(String customDesc) { this.customDesc = customDesc; }

    public String getReferenceImages() { return referenceImages; }
    public void setReferenceImages(String referenceImages) { this.referenceImages = referenceImages; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Boolean getIsWholesale() { return isWholesale; }
    public void setIsWholesale(Boolean isWholesale) { this.isWholesale = isWholesale; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public BigDecimal getFinalUnitPrice() { return finalUnitPrice; }
    public void setFinalUnitPrice(BigDecimal finalUnitPrice) { this.finalUnitPrice = finalUnitPrice; }

    public BigDecimal getFinalTotalPrice() { return finalTotalPrice; }

    public Integer getCycle() { return cycle; }
    public void setCycle(Integer cycle) { this.cycle = cycle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getDeliverContent() { return deliverContent; }
    public void setDeliverContent(String deliverContent) { this.deliverContent = deliverContent; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public LocalDateTime getAcceptTime() { return acceptTime; }
    public void setAcceptTime(LocalDateTime acceptTime) { this.acceptTime = acceptTime; }

    public LocalDateTime getFinishTime() { return finishTime; }
    public void setFinishTime(LocalDateTime finishTime) { this.finishTime = finishTime; }

    public String getMatchCreators() { return matchCreators; }
    public void setMatchCreators(String matchCreators) { this.matchCreators = matchCreators; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}