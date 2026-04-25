package com.example.handmademarket.service;

import com.example.handmademarket.dto.CreateGoodsRequest;
import com.example.handmademarket.util.ResponseResult;

public interface GoodsService {

    ResponseResult listGoods();

    ResponseResult listMyGoods(Integer currentUserId);

    ResponseResult getGoodsByCategory(String category);

    ResponseResult getGoodsByStatus(Integer status);

    ResponseResult searchGoods(String keyword);

    ResponseResult getGoods(Long id);

    ResponseResult createGoods(CreateGoodsRequest request, Integer currentUserId);

    ResponseResult updateGoods(Long id, CreateGoodsRequest request, Integer currentUserId);

    ResponseResult offlineGoods(Long id, Integer currentUserId);

    ResponseResult auditGoods(Long id, Integer status, String reason);
}
