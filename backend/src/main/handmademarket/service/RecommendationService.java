package com.example.handmademarket.service;

import com.example.handmademarket.dto.RecommendationRequest;
import com.example.handmademarket.dto.RecommendationResultDTO;
import com.example.handmademarket.entity.Goods;

public interface RecommendationService {
    // 个性化商品推荐
    RecommendationResultDTO recommendPersonalized(RecommendationRequest request);

    // 定制需求匹配推荐
    RecommendationResultDTO recommendCustomCreators(RecommendationRequest request);

    // 获取用户行为数据（浏览、收藏、订单等）
    void recordUserBehavior(String userId, String behaviorType, Object targetId);

    // 根据用户历史行为推荐商品
    RecommendationResultDTO recommendBasedOnHistory(String userId, Integer pageNum, Integer pageSize);
}