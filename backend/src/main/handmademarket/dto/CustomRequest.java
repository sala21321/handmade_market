package com.example.handmademarket.dto;

import java.math.BigDecimal;

public class CustomRequest {

    private String customDesc;
    private String referenceImages;
    private Integer quantity;
    private Boolean isWholesale;
    private BigDecimal budget;
    private Integer cycle;
    private String category;
    private String style;
    private String contact;

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

    public Integer getCycle() { return cycle; }
    public void setCycle(Integer cycle) { this.cycle = cycle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
