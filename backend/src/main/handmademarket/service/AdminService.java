package com.example.handmademarket.service;

import com.example.handmademarket.dto.AdminRequest;
import com.example.handmademarket.util.ResponseResult;

public interface AdminService {

    ResponseResult list();

    ResponseResult add(AdminRequest request);

    ResponseResult update(AdminRequest request);

    ResponseResult changeStatus(AdminRequest request, Integer status);
}