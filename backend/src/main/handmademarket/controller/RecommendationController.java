package com.example.handmademarket.controller;

import com.example.handmademarket.dto.RecommendationRequest;
import com.example.handmademarket.dto.RecommendationResultDTO;
import com.example.handmademarket.service.RecommendationService;
import com.example.handmademarket.service.UserBehaviorService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private UserBehaviorService userBehaviorService;

    /**
     * 个性化商品推荐（首页推荐使用）
     */
    @PostMapping("/personalized")
    public ResponseEntity<ResponseResult> recommendPersonalized(@RequestBody RecommendationRequest request) {
        RecommendationResultDTO result = recommendationService.recommendPersonalized(request);
        return ResponseEntity.ok(ResponseResult.ok(result));
    }

    /**
     * 基于历史记录的推荐
     */
    @GetMapping("/based-on-history/{userId}")
    public ResponseEntity<ResponseResult> recommendBasedOnHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        RecommendationResultDTO result = recommendationService.recommendBasedOnHistory(userId, pageNum, pageSize);
        return ResponseEntity.ok(ResponseResult.ok(result));
    }

    /**
     * 定制需求匹配推荐
     */
    @PostMapping("/custom-match")
    public ResponseEntity<ResponseResult> recommendCustomCreators(@RequestBody RecommendationRequest request) {
        RecommendationResultDTO result = recommendationService.recommendCustomCreators(request);
        return ResponseEntity.ok(ResponseResult.ok(result));
    }

    /**
     * 记录用户行为
     */
    @PostMapping("/record-behavior")
    public ResponseEntity<ResponseResult> recordUserBehavior(
            @RequestParam String userId,
            @RequestParam String behaviorType,
            @RequestParam Object targetId) {
        
        recommendationService.recordUserBehavior(userId, behaviorType, targetId);
        return ResponseEntity.ok(ResponseResult.ok("行为记录成功"));
    }

    /**
     * 换一批推荐（随机推荐）
     */
    @GetMapping("/refresh/{userId}")
    public ResponseEntity<ResponseResult> refreshRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 实现随机推荐逻辑
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(userId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        RecommendationResultDTO result = recommendationService.recommendPersonalized(request);
        return ResponseEntity.ok(ResponseResult.ok(result));
    }
    
    /**
     * 获取用户行为历史
     */
    @GetMapping("/user-behaviors/{userId}")
    public ResponseEntity<ResponseResult> getUserBehaviors(@PathVariable Long userId) {
        var behaviors = userBehaviorService.getUserBehaviors(userId);
        return ResponseEntity.ok(ResponseResult.ok(behaviors));
    }
    
    /**
     * 记录收藏行为
     */
    @PostMapping("/favorite")
    public ResponseEntity<ResponseResult> recordFavoriteBehavior(
            @RequestParam Long userId,
            @RequestParam Long goodsId) {
        recommendationService.recordUserBehavior(userId.toString(), "FAVORITE", goodsId);
        return ResponseEntity.ok(ResponseResult.ok("收藏行为记录成功"));
    }
    
    /**
     * 记录浏览行为
     */
    @PostMapping("/view")
    public ResponseEntity<ResponseResult> recordViewBehavior(
            @RequestParam Long userId,
            @RequestParam Long goodsId) {
        recommendationService.recordUserBehavior(userId.toString(), "VIEW", goodsId);
        return ResponseEntity.ok(ResponseResult.ok("浏览行为记录成功"));
    }
    
    /**
     * 猜你喜欢 - 专门的API端点（个人中心使用）
     */
    @GetMapping("/for-you/{userId}")
    public ResponseEntity<ResponseResult> recommendForYou(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(userId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        RecommendationResultDTO result = recommendationService.recommendPersonalized(request);
        return ResponseEntity.ok(ResponseResult.ok(result));
    }
    
    /**
     * 首页个性化推荐 - 与"猜你喜欢"完全一致的API端点
     */
    @GetMapping("/home-recommend/{userId}")
    public ResponseEntity<ResponseResult> homeRecommend(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(userId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        RecommendationResultDTO result = recommendationService.recommendPersonalized(request);
        return ResponseEntity.ok(ResponseResult.ok(result));
    }
}