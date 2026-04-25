package com.example.handmademarket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.handmademarket.dto.AdminRequest;
import com.example.handmademarket.service.AdminService;
import com.example.handmademarket.util.ResponseResult;

@RestController
@RequestMapping("/admin/manage")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 列表
    @GetMapping("/list")
    public ResponseResult list() {
        return adminService.list();
    }

    // 新增
    @PostMapping("/add")
    public ResponseResult add(@RequestBody AdminRequest request) {
        return adminService.add(request);
    }

    // 编辑
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdminRequest request) {
        return adminService.update(request);
    }

    // 禁用/启用
    @PostMapping("/status")
    public ResponseResult changeStatus(@RequestBody AdminRequest request,@RequestParam Integer status
    ) {
        return adminService.changeStatus(request, status);
    }
}