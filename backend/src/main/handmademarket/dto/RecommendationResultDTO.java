package com.example.handmademarket.dto;

import com.example.handmademarket.entity.Goods;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationResultDTO {
    private List<Goods> recommendedGoods;
    private List<Goods> matchedCreators; // 用于定制需求匹配
    private Boolean hasBehaviorData; // 是否有行为数据
    private String message; // 提示信息
    private Long total;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
}