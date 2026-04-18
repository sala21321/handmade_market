package com.example.handmademarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.handmademarket.util.ResponseResult;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<ResponseResult> getProfile() {
        // TODO: implement user profile retrieval
        return ResponseEntity.ok(ResponseResult.ok("用户信息接口骨架"));
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseResult> updateProfile() {
        // TODO: implement user profile update
        return ResponseEntity.ok(ResponseResult.ok("更新用户信息接口骨架"));
    }
}
