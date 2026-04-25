package com.example.handmademarket.service;

import com.example.handmademarket.dto.LoginRequest;
import com.example.handmademarket.dto.RegisterRequest;
import com.example.handmademarket.util.ResponseResult;

public interface AuthService {

    ResponseResult login(LoginRequest request);

    ResponseResult register(RegisterRequest request);
    
    ResponseResult resetPassword(String account, String code);
}