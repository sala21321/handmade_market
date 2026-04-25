// src/main/java/com/example/handmademarket/dto/SearchFilterDTO.java
package com.example.handmademarket.dto;

import lombok.Data;

@Data
public class SearchFilterDTO {
    private String keyword;           // 关键词
    private String category;          // 分类
    private String material;          // 材质
    private String style;             // 风格
    private Integer minPrice;         // 最小价格
    private Integer maxPrice;         // 最大价格
    private Integer minCredit;        // 最小信用分
    private Integer pageNum = 1;      // 页码
    private Integer pageSize = 10;    // 每页数量
}