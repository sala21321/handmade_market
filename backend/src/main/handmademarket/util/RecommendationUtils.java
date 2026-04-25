package com.example.handmademarket.util;

import com.example.handmademarket.entity.Goods;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationUtils {

    /**
     * 计算两个商品之间的相似度分数
     */
    public static int calculateSimilarityScore(Goods goods1, Goods goods2) {
        int score = 0;

        // 类别匹配
        if (goods1.getCategory() != null && goods1.getCategory().equals(goods2.getCategory())) {
            score += 30;
        }

        // 风格匹配
        if (goods1.getStyle() != null && goods1.getStyle().equals(goods2.getStyle())) {
            score += 20;
        }

        // 材质匹配
        if (goods1.getMaterial() != null && goods1.getMaterial().equals(goods2.getMaterial())) {
            score += 15;
        }

        // 价格相近（在30%范围内）
        if (goods1.getPrice() != null && goods2.getPrice() != null) {
            double priceDiffRatio = Math.abs(goods1.getPrice() - goods2.getPrice()) / 
                                   Math.max(goods1.getPrice(), goods2.getPrice());
            if (priceDiffRatio <= 0.3) {
                score += 10;
            }
        }

        return score;
    }

    /**
     * 根据相似度分数对商品列表进行排序
     */
    public static List<Goods> sortBySimilarity(Goods targetGoods, List<Goods> candidates) {
        return candidates.stream()
            .sorted((g1, g2) -> {
                int score1 = calculateSimilarityScore(targetGoods, g1);
                int score2 = calculateSimilarityScore(targetGoods, g2);
                return Integer.compare(score2, score1); // 降序排列
            })
            .collect(Collectors.toList());
    }

    /**
     * 计算商品集合的平均特征值
     */
    public static Map<String, Object> calculateAverageFeatures(List<Goods> goodsList) {
        // 这里可以计算平均价格、最常见类别等
        // 简化实现，返回空Map
        return Map.of();
    }
}