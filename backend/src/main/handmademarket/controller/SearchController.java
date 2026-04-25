package com.example.handmademarket.controller;

import com.example.handmademarket.dto.SearchFilterDTO;
import com.example.handmademarket.dto.SearchResultDTO;
import com.example.handmademarket.service.SearchService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 综合搜索接口 - 支持关键词搜索和条件筛选
     */
    @PostMapping("/goods")
    public ResponseResult searchGoods(@RequestBody SearchFilterDTO filter) {
        try {
            SearchResultDTO result = searchService.searchGoods(filter);
            return ResponseResult.ok(result);
        } catch (Exception e) {
            return ResponseResult.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 关键词模糊搜索接口
     */
    @GetMapping("/goods")
    public ResponseResult fuzzySearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            SearchFilterDTO filter = new SearchFilterDTO();
            filter.setKeyword(keyword);
            filter.setPageNum(pageNum);
            filter.setPageSize(pageSize);
            
            SearchResultDTO result = searchService.fuzzySearch(keyword, pageNum, pageSize);
            return ResponseResult.ok(result);
        } catch (Exception e) {
            return ResponseResult.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 条件筛选接口
     */
    @PostMapping("/filter")
    public ResponseResult filterGoods(@RequestBody SearchFilterDTO filter) {
        try {
            SearchResultDTO result = searchService.filterGoods(filter);
            return ResponseResult.ok(result);
        } catch (Exception e) {
            return ResponseResult.fail("筛选失败: " + e.getMessage());
        }
    }

    /**
     * 搜索建议接口 - 根据输入关键词提供搜索建议
     */
    @GetMapping("/suggestions")
    public ResponseResult getSearchSuggestions(@RequestParam String keyword) {
        // 这里可以根据实际需求实现搜索建议功能
        // 比如返回匹配的部分商品名称等
        String[] suggestions = {"手工饰品", "陶瓷制品", "编织用品", "木工制品"};
        return ResponseResult.ok(suggestions);
    }
}