// src/main/java/com/example/handmademarket/service/impl/SearchServiceImpl.java
package com.example.handmademarket.service.impl;

import com.example.handmademarket.dto.SearchFilterDTO;
import com.example.handmademarket.dto.SearchResultDTO;
import com.example.handmademarket.entity.Goods;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.GoodsRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private GoodsRepository goodsRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public SearchResultDTO searchGoods(SearchFilterDTO filter) {
        // 处理空字符串
        String keyword = (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) ? filter.getKeyword() : null;
        String category = (filter.getCategory() != null && !filter.getCategory().isEmpty()) ? filter.getCategory() : null;
        String material = (filter.getMaterial() != null && !filter.getMaterial().isEmpty()) ? filter.getMaterial() : null;
        String style = (filter.getStyle() != null && !filter.getStyle().isEmpty()) ? filter.getStyle() : null;
        Integer minPrice = (filter.getMinPrice() != null && filter.getMinPrice() > 0) ? filter.getMinPrice() : null;
        Integer maxPrice = (filter.getMaxPrice() != null && filter.getMaxPrice() > 0) ? filter.getMaxPrice() : null;
        
        // 获取所有匹配的商品
        List<Goods> allMatchingGoods = goodsRepository.searchGoodsWithoutPaging(
            keyword,
            category,
            material,
            style,
            minPrice,
            maxPrice
        );
        
        // 根据相关性评分排序
        List<Goods> sortedGoods = allMatchingGoods.stream()
            .map(goods -> {
                int relevanceScore = calculateRelevanceScore(goods, keyword);
                goods.setSortWeight(relevanceScore); // 假设我们添加了一个临时字段
                return goods;
            })
            .sorted((g1, g2) -> Integer.compare(g2.getSortWeight(), g1.getSortWeight())) // 降序排列
            .collect(Collectors.toList());
        
        // 应用分页
        int pageNum = filter.getPageNum() - 1;
        int pageSize = filter.getPageSize();
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, sortedGoods.size());
        
        List<Goods> pagedGoods;
        if (startIndex >= sortedGoods.size()) {
            pagedGoods = new ArrayList<>();
        } else {
            pagedGoods = sortedGoods.subList(startIndex, endIndex);
        }
        
        SearchResultDTO result = new SearchResultDTO();
        result.setGoodsList(pagedGoods);
        result.setTotal((long) sortedGoods.size());
        result.setCurrentPage(filter.getPageNum());
        result.setPageSize(filter.getPageSize());
        result.setTotalPages((int) Math.ceil((double) sortedGoods.size() / filter.getPageSize()));
        
        return result;
    }

    // 计算相关性评分
    private int calculateRelevanceScore(Goods goods, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            // 如果没有关键词，则按发布时间排序
            return 0;
        }
        
        int score = 0;
        String lowerKeyword = keyword.toLowerCase();
        
        // 商品名称匹配（最高权重）
        if (goods.getGoodsName() != null && goods.getGoodsName().toLowerCase().contains(lowerKeyword)) {
            score += 10;
        }
        
        // 材质匹配
        if (goods.getMaterial() != null && goods.getMaterial().toLowerCase().contains(lowerKeyword)) {
            score += 5;
        }
        
        // 风格匹配
        if (goods.getStyle() != null && goods.getStyle().toLowerCase().contains(lowerKeyword)) {
            score += 3;
        }
        
        // 详情匹配
        if (goods.getDetails() != null && goods.getDetails().toLowerCase().contains(lowerKeyword)) {
            score += 1;
        }
        
        return score;
    }

    @Override
    public SearchResultDTO fuzzySearch(String keyword, int pageNum, int pageSize) {
        // 处理空字符串
        String processedKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        
        List<Goods> allMatchingGoods = goodsRepository.searchGoodsWithoutPaging(
            processedKeyword, null, null, null, null, null
        );
        
        // 根据相关性评分排序
        List<Goods> sortedGoods = allMatchingGoods.stream()
            .map(goods -> {
                int relevanceScore = calculateRelevanceScore(goods, processedKeyword);
                goods.setSortWeight(relevanceScore);
                return goods;
            })
            .sorted((g1, g2) -> Integer.compare(g2.getSortWeight(), g1.getSortWeight())) // 降序排列
            .collect(Collectors.toList());
        
        // 应用分页
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, sortedGoods.size());
        
        List<Goods> pagedGoods;
        if (startIndex >= sortedGoods.size()) {
            pagedGoods = new ArrayList<>();
        } else {
            pagedGoods = sortedGoods.subList(startIndex, endIndex);
        }
        
        SearchResultDTO result = new SearchResultDTO();
        result.setGoodsList(pagedGoods);
        result.setTotal((long) sortedGoods.size());
        result.setCurrentPage(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages((int) Math.ceil((double) sortedGoods.size() / pageSize));
        
        return result;
    }

    @Override
    
    public SearchResultDTO filterGoods(SearchFilterDTO filter) {
        // 处理空字符串
        String keyword = (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) ? filter.getKeyword() : null;
        String category = (filter.getCategory() != null && !filter.getCategory().isEmpty()) ? filter.getCategory() : null;
        String material = (filter.getMaterial() != null && !filter.getMaterial().isEmpty()) ? filter.getMaterial() : null;
        String style = (filter.getStyle() != null && !filter.getStyle().isEmpty()) ? filter.getStyle() : null;
        Integer minPrice = (filter.getMinPrice() != null && filter.getMinPrice() > 0) ? filter.getMinPrice() : null;
        Integer maxPrice = (filter.getMaxPrice() != null && filter.getMaxPrice() > 0) ? filter.getMaxPrice() : null;
        
        // 获取所有匹配的商品
        List<Goods> allMatchingGoods = goodsRepository.searchGoodsWithoutPaging(
            keyword,
            category,
            material,
            style,
            minPrice,
            maxPrice
        );
        
        // 根据相关性评分排序
        List<Goods> sortedGoods = allMatchingGoods.stream()
            .map(goods -> {
                int relevanceScore = calculateRelevanceScore(goods, keyword);
                goods.setSortWeight(relevanceScore);
                return goods;
            })
            .sorted((g1, g2) -> Integer.compare(g2.getSortWeight(), g1.getSortWeight())) // 降序排列
            .collect(Collectors.toList());
        
        // 如果有信用分筛选，则进一步过滤结果
        if (filter.getMinCredit() != null) {
            List<User> eligibleUsers = userRepository.findByCreditScoreGreaterThanEqual(filter.getMinCredit());
            List<Long> eligibleCreatorIds = eligibleUsers.stream()
                .map(user -> user.getUserId().longValue())
                .collect(Collectors.toList());
            
            if (!eligibleCreatorIds.isEmpty()) {
                sortedGoods = sortedGoods.stream()
                    .filter(goods -> eligibleCreatorIds.contains(goods.getCreatorId()))
                    .collect(Collectors.toList());
            } else {
                sortedGoods = new ArrayList<>(); // 如果没有符合条件的创作者，返回空列表
            }
        }
        
        // 应用分页
        int pageNum = filter.getPageNum() - 1;
        int pageSize = filter.getPageSize();
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, sortedGoods.size());
        
        List<Goods> pagedGoods;
        if (startIndex >= sortedGoods.size()) {
            pagedGoods = new ArrayList<>();
        } else {
            pagedGoods = sortedGoods.subList(startIndex, endIndex);
        }
        
        SearchResultDTO result = new SearchResultDTO();
        result.setGoodsList(pagedGoods);
        result.setTotal((long) sortedGoods.size());
        result.setCurrentPage(filter.getPageNum());
        result.setPageSize(filter.getPageSize());
        result.setTotalPages((int) Math.ceil((double) sortedGoods.size() / filter.getPageSize()));
        
        return result;
    }
}