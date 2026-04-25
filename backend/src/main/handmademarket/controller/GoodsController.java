package com.example.handmademarket.controller;

import com.example.handmademarket.dto.CreateGoodsRequest;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.util.ResponseResult;
import com.example.handmademarket.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.handmademarket.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    private Integer extractCurrentUserId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录凭证格式错误");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        String userAccount = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUserAccount(userAccount)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "当前用户不存在"));
        return user.getUserId();
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseResult> listGoods() {
        return ResponseEntity.ok(goodsService.listGoods());
    }

    @GetMapping("/my-goods")
    public ResponseEntity<ResponseResult> listMyGoods(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer currentUserId = extractCurrentUserId(authHeader);
        return ResponseEntity.ok(goodsService.listMyGoods(currentUserId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ResponseResult> getGoodsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(goodsService.getGoodsByCategory(category));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseResult> getGoodsByStatus(@PathVariable Integer status) {
        return ResponseEntity.ok(goodsService.getGoodsByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseResult> searchGoods(@RequestParam String keyword) {
        return ResponseEntity.ok(goodsService.searchGoods(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult> getGoods(@PathVariable Long id) {
        return ResponseEntity.ok(goodsService.getGoods(id));
    }

    @PostMapping
    public ResponseEntity<ResponseResult> createGoods(
            @RequestBody CreateGoodsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer currentUserId = extractCurrentUserId(authHeader);
        return ResponseEntity.ok(goodsService.createGoods(request, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult> updateGoods(
            @PathVariable Long id,
            @RequestBody CreateGoodsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer currentUserId = extractCurrentUserId(authHeader);
        return ResponseEntity.ok(goodsService.updateGoods(id, request, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseResult> offlineGoods(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Integer currentUserId = extractCurrentUserId(authHeader);
        return ResponseEntity.ok(goodsService.offlineGoods(id, currentUserId));
    }

    @PutMapping("/{id}/audit")
    public ResponseEntity<ResponseResult> auditGoods(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(goodsService.auditGoods(id, status, reason));
    }
}
