// src/main/java/com/example/handmademarket/service/SearchService.java
package com.example.handmademarket.service;

import com.example.handmademarket.dto.SearchFilterDTO;
import com.example.handmademarket.dto.SearchResultDTO;
import com.example.handmademarket.entity.Goods;

public interface SearchService {
    SearchResultDTO searchGoods(SearchFilterDTO filter);
    
    // 模糊搜索商品
    SearchResultDTO fuzzySearch(String keyword, int pageNum, int pageSize);
    
    // 条件筛选商品
    SearchResultDTO filterGoods(SearchFilterDTO filter);
}