package com.example.handmademarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.handmademarket.util.ResponseResult;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @GetMapping
    public ResponseEntity<ResponseResult> listGoods() {
        // TODO: implement goods list and search
        return ResponseEntity.ok(ResponseResult.ok("商品列表接口骨架"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult> getGoods(@PathVariable Long id) {
        // TODO: implement goods detail fetch
        return ResponseEntity.ok(ResponseResult.ok("商品详情接口骨架"));
    }

    @PostMapping
    public ResponseEntity<ResponseResult> createGoods() {
        // TODO: implement publish goods logic
        return ResponseEntity.ok(ResponseResult.ok("发布商品接口骨架"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult> updateGoods(@PathVariable Long id) {
        // TODO: implement update goods logic
        return ResponseEntity.ok(ResponseResult.ok("修改商品接口骨架"));
    }
}
