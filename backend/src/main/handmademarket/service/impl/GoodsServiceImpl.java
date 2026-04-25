package com.example.handmademarket.service.impl;

import com.example.handmademarket.dto.CreateGoodsRequest;
import com.example.handmademarket.entity.Goods;
import com.example.handmademarket.repository.GoodsRepository;
import com.example.handmademarket.service.GoodsService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsRepository goodsRepository;

    private String normalizeImages(String rawImages) {
        if (rawImages == null) {
            return null;
        }
        return rawImages.trim();
    }

    @Override
    public ResponseResult listGoods() {
        return ResponseResult.ok(goodsRepository.findAll());
    }

    @Override
    public ResponseResult listMyGoods(Integer currentUserId) {
        if (currentUserId == null) {
            return ResponseResult.fail("请先登录");
        }
        return ResponseResult.ok(goodsRepository.findByCreatorIdOrderByPublishTimeDesc(currentUserId.longValue()));
    }

    @Override
    public ResponseResult getGoods(Long id) {
        Optional<Goods> goods = goodsRepository.findById(id);
        if (goods.isEmpty()) {
            return ResponseResult.fail("商品不存在");
        }
        return ResponseResult.ok(goods.get());
    }

    @Override
    public ResponseResult createGoods(CreateGoodsRequest request, Integer currentUserId) {
        if (request == null) {
            return ResponseResult.fail("请求参数不能为空");
        }

        if (currentUserId == null) {
            return ResponseResult.fail("请先登录后再发布商品");
        }

        if (request.getGoodsName() == null || request.getGoodsName().isEmpty()) {
            return ResponseResult.fail("商品名称不能为空");
        }

        if (request.getPrice() == null || request.getPrice() <= 0) {
            return ResponseResult.fail("商品价格必须大于0");
        }

        Goods goods = new Goods();
        goods.setCreatorId(currentUserId.longValue());
        goods.setGoodsName(request.getGoodsName());
        goods.setTitle(request.getGoodsName());
        goods.setPrice(request.getPrice());
        goods.setReservePrice(request.getReservePrice());
        goods.setMaterial(request.getMaterial());
        goods.setSize(request.getSize());
        goods.setStyle(request.getStyle());
        goods.setDeliveryCycle(request.getDeliveryCycle());
        goods.setDetails(request.getDetails());
        String normalizedImages = normalizeImages(request.getImages());
        goods.setImages(normalizedImages);
        goods.setImageUrl(normalizedImages);
        goods.setCategory(request.getCategory());
        goods.setStock(request.getStock() != null ? request.getStock() : 0);
        goods.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        goods.setPublishTime(LocalDateTime.now());

        Goods savedGoods = goodsRepository.save(goods);
        return ResponseResult.ok(savedGoods);
    }

    @Override
    public ResponseResult updateGoods(Long id, CreateGoodsRequest request, Integer currentUserId) {
        if (currentUserId == null) {
            return ResponseResult.fail("请先登录后再修改商品");
        }
        Optional<Goods> goods = goodsRepository.findById(id);
        if (goods.isEmpty()) {
            return ResponseResult.fail("商品不存在");
        }
        Goods good = goods.get();
        if (good.getCreatorId() == null || !good.getCreatorId().equals(currentUserId.longValue())) {
            return ResponseResult.fail("只能修改自己的商品");
        }
        good.setGoodsName(request.getGoodsName());
        good.setTitle(request.getGoodsName());
        good.setPrice(request.getPrice());
        good.setReservePrice(request.getReservePrice());
        good.setMaterial(request.getMaterial());
        good.setSize(request.getSize());
        good.setStyle(request.getStyle());
        good.setDeliveryCycle(request.getDeliveryCycle());
        good.setDetails(request.getDetails());
        String normalizedImages = normalizeImages(request.getImages());
        good.setImages(normalizedImages);
        good.setImageUrl(normalizedImages);
        good.setCategory(request.getCategory());
        if (request.getStock() != null) {
            good.setStock(request.getStock());
        }
        if (request.getStatus() != null) {
            good.setStatus(request.getStatus());
            if (request.getStatus() == 0) {
                good.setAuditRemark(null);
            }
        }
        Goods updated = goodsRepository.save(good);
        return ResponseResult.ok("商品更新成功", updated);
    }

    @Override
    public ResponseResult offlineGoods(Long id, Integer currentUserId) {
        if (currentUserId == null) {
            return ResponseResult.fail("请先登录后再下架商品");
        }
        Optional<Goods> goods = goodsRepository.findById(id);
        if (goods.isEmpty()) {
            return ResponseResult.fail("商品不存在");
        }
        Goods good = goods.get();
        if (good.getCreatorId() == null || !good.getCreatorId().equals(currentUserId.longValue())) {
            return ResponseResult.fail("只能下架自己的商品");
        }
        good.setStatus(3);
        goodsRepository.save(good);
        return ResponseResult.ok("商品已下架");
    }

    @Override
    public ResponseResult getGoodsByCategory(String category) {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Goods> goods = goodsRepository.findByCategory(category, pageable);
        return ResponseResult.ok(goods);
    }

    @Override
    public ResponseResult getGoodsByStatus(Integer status) {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Goods> goods = goodsRepository.findByStatus(status, pageable);
        return ResponseResult.ok(goods);
    }

    @Override
    public ResponseResult searchGoods(String keyword) {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Goods> goods = goodsRepository.searchGoods(keyword, pageable);
        return ResponseResult.ok(goods);
    }

    @Override
    public ResponseResult auditGoods(Long id, Integer status, String reason) {
        Optional<Goods> goods = goodsRepository.findById(id);
        if (goods.isEmpty()) {
            return ResponseResult.fail("商品不存在");
        }
        Goods good = goods.get();
        good.setStatus(status);
        good.setAuditTime(LocalDateTime.now());
        good.setAuditRemark(reason);
        goodsRepository.save(good);
        return ResponseResult.ok("审核完成");
    }
}
