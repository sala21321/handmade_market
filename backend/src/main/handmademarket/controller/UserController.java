package com.example.handmademarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.handmademarket.dto.UserUpdateRequest;
import com.example.handmademarket.service.UserService;
import com.example.handmademarket.util.ResponseResult;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 获取个人信息
    @GetMapping("/info")
    public ResponseEntity<ResponseResult> info() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseResult> update(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

}

