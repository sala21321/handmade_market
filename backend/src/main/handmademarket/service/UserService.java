package com.example.handmademarket.service;

import com.example.handmademarket.dto.UserUpdateRequest;
import com.example.handmademarket.util.ResponseResult;

public interface UserService {

    ResponseResult getProfile();

    ResponseResult updateProfile(UserUpdateRequest request);

}