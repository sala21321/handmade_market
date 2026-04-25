package com.example.handmademarket.service.impl;

import com.example.handmademarket.dto.RecommendationRequest;
import com.example.handmademarket.dto.RecommendationResultDTO;
import com.example.handmademarket.entity.Goods;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.entity.UserBehavior;
import com.example.handmademarket.repository.GoodsRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.RecommendationService;
import com.example.handmademarket.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserBehaviorService userBehaviorService;

    @Override
    public RecommendationResultDTO recommendPersonalized(RecommendationRequest request) {
        RecommendationResultDTO result = new RecommendationResultDTO();
        Long userId = parseUserId(request.getUserId());

        // 检查用户是否存在行为数据
        boolean hasBehaviorData = checkUserBehaviorData(userId.toString());

        if (!hasBehaviorData) {
            // 无行为数据的消费者，不展示个性化推荐
            result.setHasBehaviorData(false);
            result.setMessage("先浏览商品解锁推荐");
            result.setRecommendedGoods(new ArrayList<>());
            result.setTotal(0L);
            result.setCurrentPage(request.getPageNum());
            result.setPageSize(request.getPageSize());
            result.setTotalPages(0);
            return result;
        }

        // 有行为数据，执行个性化推荐
        List<Goods> recommendedGoods = performPersonalizedRecommendation(userId, request);

        // 分页处理
        int pageNum = request.getPageNum() - 1;
        int pageSize = request.getPageSize();
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, recommendedGoods.size());

        List<Goods> pagedGoods;
        if (startIndex >= recommendedGoods.size()) {
            pagedGoods = new ArrayList<>();
        } else {
            pagedGoods = recommendedGoods.subList(startIndex, endIndex);
        }

        result.setHasBehaviorData(true);
        result.setMessage("个性化推荐");
        result.setRecommendedGoods(pagedGoods);
        result.setTotal((long) recommendedGoods.size());
        result.setCurrentPage(request.getPageNum());
        result.setPageSize(request.getPageSize());
        result.setTotalPages((int) Math.ceil((double) recommendedGoods.size() / pageSize));

        return result;
    }

    @Override
    public RecommendationResultDTO recommendCustomCreators(RecommendationRequest request) {
        RecommendationResultDTO result = new RecommendationResultDTO();

        // 提取定制需求特征
        String category = request.getCategory();
        String style = request.getStyle();
        String material = request.getMaterial();

        // 检查是否有足够的特征信息
        boolean hasSufficientFeatures = (category != null && !category.isEmpty()) ||
                (style != null && !style.isEmpty()) ||
                (material != null && !material.isEmpty());

        if (!hasSufficientFeatures) {
            // 特征缺失时，减少匹配创作者数量
            result.setMessage("请完善定制需求信息以获得更精准的匹配");
            result.setMatchedCreators(new ArrayList<>());
            result.setTotal(0L);
            result.setCurrentPage(request.getPageNum());
            result.setPageSize(request.getPageSize());
            result.setTotalPages(0);
            return result;
        }

        // 执行创作者匹配
        List<Goods> matchedCreators = performCreatorMatching(category, style, material, request);

        // 分页处理
        int pageNum = request.getPageNum() - 1;
        int pageSize = request.getPageSize();
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, matchedCreators.size());

        List<Goods> pagedCreators;
        if (startIndex >= matchedCreators.size()) {
            pagedCreators = new ArrayList<>();
        } else {
            pagedCreators = matchedCreators.subList(startIndex, endIndex);
        }

        if (pagedCreators.isEmpty()) {
            result.setMessage("暂无匹配的创作者，请调整需求条件或等待创作者入驻");
        } else {
            result.setMessage("匹配的创作者");
        }

        result.setMatchedCreators(pagedCreators);
        result.setTotal((long) matchedCreators.size());
        result.setCurrentPage(request.getPageNum());
        result.setPageSize(request.getPageSize());
        result.setTotalPages((int) Math.ceil((double) matchedCreators.size() / pageSize));

        return result;
    }

    @Override
    public void recordUserBehavior(String userId, String behaviorType, Object targetId) {
        // 实际记录用户行为到数据库
        Long userIdLong = parseUserId(userId);
        Long targetIdLong = parseTargetId(targetId);
        userBehaviorService.saveBehavior(userIdLong, targetIdLong, behaviorType);
    }

    @Override
    public RecommendationResultDTO recommendBasedOnHistory(String userId, Integer pageNum, Integer pageSize) {
        RecommendationResultDTO result = new RecommendationResultDTO();

        // 获取用户的历史行为数据
        List<Goods> userHistory = getUserHistory(parseUserId(userId));

        if (userHistory.isEmpty()) {
            result.setHasBehaviorData(false);
            result.setMessage("暂无浏览历史，无法推荐");
            result.setRecommendedGoods(new ArrayList<>());
            result.setTotal(0L);
            result.setCurrentPage(pageNum);
            result.setPageSize(pageSize);
            result.setTotalPages(0);
            return result;
        }

        // 基于历史行为推荐相似商品
        Set<Goods> recommendations = new HashSet<>();

        // 根据用户浏览过的商品类别、风格、材质推荐相似商品
        for (Goods historyItem : userHistory) {
            // 获取同类商品
            List<Goods> similarByCategory = goodsRepository.findByCategory(historyItem.getCategory(), 
                PageRequest.of(0, 5)).getContent();
            recommendations.addAll(similarByCategory);

            // 获取同风格商品
            if (historyItem.getStyle() != null) {
                List<Goods> similarByStyle = goodsRepository.searchByKeyword(historyItem.getStyle()).stream()
                    .limit(5)
                    .collect(Collectors.toList());
                recommendations.addAll(similarByStyle);
            }

            // 获取同材质商品
            if (historyItem.getMaterial() != null) {
                List<Goods> similarByMaterial = goodsRepository.searchByKeyword(historyItem.getMaterial()).stream()
                    .limit(5)
                    .collect(Collectors.toList());
                recommendations.addAll(similarByMaterial);
            }
        }

        // 按匹配度排序
        List<Goods> sortedRecommendations = recommendations.stream()
            .sorted((g1, g2) -> {
                // 计算与用户历史的匹配度
                int score1 = calculateMatchScore(g1, userHistory);
                int score2 = calculateMatchScore(g2, userHistory);
                return Integer.compare(score2, score1); // 降序排列
            })
            .collect(Collectors.toList());

        // 分页处理
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, sortedRecommendations.size());

        List<Goods> pagedRecommendations;
        if (startIndex >= sortedRecommendations.size()) {
            pagedRecommendations = new ArrayList<>();
        } else {
            pagedRecommendations = sortedRecommendations.subList(startIndex, endIndex);
        }

        result.setHasBehaviorData(true);
        result.setMessage("基于历史的推荐");
        result.setRecommendedGoods(pagedRecommendations);
        result.setTotal((long) sortedRecommendations.size());
        result.setCurrentPage(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages((int) Math.ceil((double) sortedRecommendations.size() / pageSize));

        return result;
    }

    // 检查用户是否有行为数据
    private boolean checkUserBehaviorData(String userId) {
        // 从数据库中查询用户行为记录
        Long userIdLong = parseUserId(userId);
        List<UserBehavior> behaviors = userBehaviorService.getUserBehaviors(userIdLong);
        return !behaviors.isEmpty();
    }

    // 执行个性化推荐
    private List<Goods> performPersonalizedRecommendation(Long userId, RecommendationRequest request) {
        // 获取用户的历史行为
        List<Goods> userHistory = getUserHistory(userId);

        // 获取所有已上架商品
        Pageable pageable = PageRequest.of(0, 100); // 限制数量避免性能问题
        Page<Goods> allGoodsPage = goodsRepository.findByStatus(1, pageable);
        List<Goods> allGoods = allGoodsPage.getContent();

        // 计算每个商品的推荐分数
        Map<Goods, Integer> scores = new HashMap<>();
        for (Goods goods : allGoods) {
            int score = calculateRecommendationScore(goods, userHistory);
            if (score > 0) {
                scores.put(goods, score);
            }
        }

        // 按分数排序并返回
        return scores.entrySet().stream()
            .sorted(Map.Entry.<Goods, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .filter(goods -> !userHistory.contains(goods)) // 排除已交互过的商品
            .limit(50) // 限制推荐数量
            .collect(Collectors.toList());
    }

    // 计算推荐分数
    private int calculateRecommendationScore(Goods goods, List<Goods> userHistory) {
        int score = 0;

        for (Goods historyItem : userHistory) {
            // 匹配类别
            if (goods.getCategory() != null && historyItem.getCategory() != null &&
                goods.getCategory().equals(historyItem.getCategory())) {
                score += 20;
            }

            // 匹配风格
            if (goods.getStyle() != null && historyItem.getStyle() != null &&
                goods.getStyle().equals(historyItem.getStyle())) {
                score += 15;
            }

            // 匹配材质
            if (goods.getMaterial() != null && historyItem.getMaterial() != null &&
                goods.getMaterial().equals(historyItem.getMaterial())) {
                score += 10;
            }

            // 匹配价格区间（在用户历史商品价格的±30%范围内）
            if (goods.getPrice() != null && historyItem.getPrice() != null) {
                if (historyItem.getPrice() > 0) { // 避免除零错误
                    double priceDiff = Math.abs(goods.getPrice() - historyItem.getPrice()) / historyItem.getPrice();
                    if (priceDiff <= 0.3) {
                        score += 5;
                    }
                }
            }
        }

        return score;
    }

    // 执行创作者匹配
    private List<Goods> performCreatorMatching(String category, String style, String material, RecommendationRequest request) {
        List<Goods> matchedGoods = new ArrayList<>();

        // 按类别匹配
        if (category != null && !category.isEmpty()) {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Goods> goodsByCategory = goodsRepository.findByCategory(category, pageable);
            matchedGoods.addAll(goodsByCategory.getContent());
        }

        // 按风格匹配
        if (style != null && !style.isEmpty()) {
            List<Goods> goodsByStyle = goodsRepository.searchByKeyword(style).stream()
                .limit(20)
                .collect(Collectors.toList());
            matchedGoods.addAll(goodsByStyle);
        }

        // 按材质匹配
        if (material != null && !material.isEmpty()) {
            List<Goods> goodsByMaterial = goodsRepository.searchByKeyword(material).stream()
                .limit(20)
                .collect(Collectors.toList());
            matchedGoods.addAll(goodsByMaterial);
        }

        // 去重并按创作者信用分和销量排序
        Set<Goods> uniqueGoods = new LinkedHashSet<>(matchedGoods);
        List<Goods> sortedGoods = new ArrayList<>(uniqueGoods);

        sortedGoods.sort((g1, g2) -> {
            // 按创作者信用分排序
            int creditScoreComparison = compareByCreatorCredit(g1, g2);
            if (creditScoreComparison != 0) {
                return creditScoreComparison;
            }

            // 信用分相同时按销量排序
            return compareBySalesVolume(g1, g2);
        });

        return sortedGoods;
    }

    // 比较创作者信用分
    private int compareByCreatorCredit(Goods g1, Goods g2) {
        // 获取创作者信息并比较信用分
        // 实际实现中需要关联User表查询信用分
        User creator1 = userRepository.findById(g1.getCreatorId()).orElse(null);
        User creator2 = userRepository.findById(g2.getCreatorId()).orElse(null);
        if (creator1 != null && creator2 != null) {
            Integer score1 = creator1.getCreditScore();
            Integer score2 = creator2.getCreditScore();
            if (score1 != null && score2 != null) {
                return Integer.compare(score2, score1); // 降序排列
            }
        } else if (creator1 != null) {
            return -1; // creator1排在前面
        } else if (creator2 != null) {
            return 1;  // creator2排在前面
        }
        return 0; // 相等
    }

    // 比较销量
    private int compareBySalesVolume(Goods g1, Goods g2) {
        // 实际实现中需要统计商品销量
        // 这里简化处理，返回0表示相等
        return 0;
    }

    // 获取用户历史行为数据
    private List<Goods> getUserHistory(Long userId) {
        // 从用户行为表中获取用户的历史行为数据
        List<Long> goodsIds = userBehaviorService.getUserInteractedGoodsIds(userId);
        List<Goods> goodsList = new ArrayList<>();
        for (Long goodsId : goodsIds) {
            Optional<Goods> goodsOpt = goodsRepository.findById(goodsId);
            if (goodsOpt.isPresent()) {
                goodsList.add(goodsOpt.get());
            }
        }
        return goodsList;
    }

    // 计算匹配分数
    private int calculateMatchScore(Goods goods, List<Goods> userHistory) {
        int score = 0;
        for (Goods historyItem : userHistory) {
            if (goods.getCategory() != null && goods.getCategory().equals(historyItem.getCategory())) {
                score += 10;
            }
            if (goods.getStyle() != null && goods.getStyle().equals(historyItem.getStyle())) {
                score += 8;
            }
            if (goods.getMaterial() != null && goods.getMaterial().equals(historyItem.getMaterial())) {
                score += 6;
            }
        }
        return score;
    }
    
    // 解析用户ID字符串为Long类型
    private Long parseUserId(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            // 如果转换失败，返回默认值或抛出异常
            throw new IllegalArgumentException("Invalid user ID: " + userIdStr);
        }
    }
    
    // 解析目标ID对象为Long类型
    private Long parseTargetId(Object targetIdObj) {
        if (targetIdObj instanceof String) {
            try {
                return Long.parseLong((String) targetIdObj);
            } catch (NumberFormatException e) {
                return -1L; // 默认值
            }
        } else if (targetIdObj instanceof Integer) {
            return ((Integer) targetIdObj).longValue();
        } else if (targetIdObj instanceof Long) {
            return (Long) targetIdObj;
        } else {
            return -1L; // 默认值
        }
    }
}