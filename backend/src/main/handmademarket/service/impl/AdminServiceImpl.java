package com.example.handmademarket.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.handmademarket.dto.AdminRequest;
import com.example.handmademarket.entity.Admin;
import com.example.handmademarket.repository.AdminRepository;
import com.example.handmademarket.service.AdminService;
import com.example.handmademarket.util.ResponseResult;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseResult list() {
        List<Admin> list = adminRepository.findAll();
        return ResponseResult.ok(list);
    }

    @Override
    public ResponseResult add(AdminRequest request) {
        Optional<Admin> exist = adminRepository.findByAdminAccount(request.getAdminAccount());
        if (exist.isPresent()) {
            return ResponseResult.fail("账号已存在");
        }

        Admin admin = new Admin();
        admin.setAdminAccount(request.getAdminAccount());
        admin.setAdminPwd(passwordEncoder.encode(request.getAdminPwd()));
        admin.setAdminName(request.getAdminName());
        admin.setPermissionLevel(request.getPermissionLevel());
        admin.setStatus(1);
        admin.setCreateTime(new Date());
        admin.setUpdateTime(new Date());

        adminRepository.save(admin);
        return ResponseResult.ok("新增成功");
    }

    @Override
    public ResponseResult update(AdminRequest request) {
        Optional<Admin> optional = adminRepository.findById(Long.valueOf(request.getAdminId()));
        if (optional.isEmpty()) {
            return ResponseResult.fail("管理员不存在");
        }

        Admin admin = optional.get();
        admin.setAdminName(request.getAdminName());
        admin.setPermissionLevel(request.getPermissionLevel());
        admin.setUpdateTime(new Date());

        if (request.getAdminPwd() != null && !request.getAdminPwd().isEmpty()) {
            admin.setAdminPwd(passwordEncoder.encode(request.getAdminPwd()));
        }

        adminRepository.save(admin);
        return ResponseResult.ok("修改成功");
    }

    @Override
    public ResponseResult changeStatus(AdminRequest request, Integer status) {
        Optional<Admin> optional = adminRepository.findById(Long.valueOf(request.getAdminId()));
        if (optional.isEmpty()) {
            return ResponseResult.fail("管理员不存在");
        }

        Admin admin = optional.get();

        if (status == 0 && admin.getPermissionLevel() == 1) {
            long count = adminRepository.countByPermissionLevelAndStatus(1, 1);
            if (count <= 1) {
                return ResponseResult.fail("禁止禁用最后一位超级管理员");
            }
        }

        admin.setStatus(status);
        admin.setUpdateTime(new Date());
        adminRepository.save(admin);

        return ResponseResult.ok(status == 0 ? "已禁用" : "已启用");
    }
}