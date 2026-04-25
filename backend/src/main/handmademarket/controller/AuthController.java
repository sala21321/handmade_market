package com.example.handmademarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.handmademarket.dto.LoginRequest;
import com.example.handmademarket.dto.PasswordResetRequest;
import com.example.handmademarket.dto.RegisterRequest;
import com.example.handmademarket.service.AuthService;
import com.example.handmademarket.util.ResponseResult;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseResult> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ResponseResult> resetPassword(@RequestBody PasswordResetRequest request) {

        // 固定验证码：直接用类里的 a123456
        if (!PasswordResetRequest.FIXED_CODE.equals(request.getCode())) {
            return ResponseEntity.ok(ResponseResult.fail("验证码错误"));
        }

        // 重置密码（账号 + 新密码）
        return ResponseEntity.ok(authService.resetPassword(
                request.getAccount(),
                request.getNewPassword()));
    }
}