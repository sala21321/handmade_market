package com.example.handmademarket.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.handmademarket.dto.LoginRequest;
import com.example.handmademarket.dto.RegisterRequest;
import com.example.handmademarket.entity.Admin;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.AdminRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.AuthService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final String DEFAULT_PASSWORD = "a123456";

    public AuthServiceImpl(UserRepository userRepository, AdminRepository adminRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private String generateUniqueUserAccount() {
        return "user" + System.currentTimeMillis();
    }

    @Override
    public ResponseResult login(LoginRequest request) {
        String userAccount = request.getUserAccount() == null ? "" : request.getUserAccount().replaceAll("\\s+", "");
        String password = request.getPassword() == null ? "" : request.getPassword().trim();

        if (userAccount.isEmpty() || password.isEmpty()) {
            return ResponseResult.fail("账号密码不能为空");
        }

        if (request.getType() == 1) {
            return userLogin(request);
        } else if (request.getType() == 2) {
            return adminLogin(request);
        }
        return ResponseResult.fail("登录类型错误");
    }

    private ResponseResult userLogin(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUserAccountOrPhone(
                request.getUserAccount(),
                request.getUserAccount());
        if (userOptional.isEmpty()) {
            return ResponseResult.fail("账号不存在");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseResult.fail("密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserAccount());
        Map<String, Object> map = new HashMap<>();
        map.put("userAccount", user.getUserAccount());
        map.put("token", token);
        return ResponseResult.ok("登录成功", map);
    }

    private ResponseResult adminLogin(LoginRequest request) {
        Optional<Admin> adminOptional = adminRepository.findByAdminAccount(request.getUserAccount());
        if (adminOptional.isEmpty()) {
            return ResponseResult.fail("管理员账号不存在");
        }
        Admin admin = adminOptional.get();

        if (!request.getPassword().equals(admin.getAdminPwd())) {
            return ResponseResult.fail("密码错误");
        }

        String token = jwtUtil.generateToken(admin.getAdminAccount());
        Map<String, Object> map = new HashMap<>();
        map.put("userAccount", admin.getAdminAccount());
        map.put("token", token);
        return ResponseResult.ok("登录成功", map);
    }

    // ==============================
    // ✅【修复完成】注册 100% 成功
    // ==============================
    @Override
    public ResponseResult register(RegisterRequest request) {
        String phone = request.getPhone().trim();
        String password = request.getPassword().trim();

        // 基础校验
        if (phone.isEmpty() || password.isEmpty()) {
            return ResponseResult.fail("手机号和密码不能为空");
        }
        if (phone.length() != 11) {
            return ResponseResult.fail("手机号格式错误");
        }
        if (userRepository.existsByPhone(phone)) {
            return ResponseResult.fail("该手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setUserAccount(phone); // 账号 = 手机号（登录直接用手机号）
        user.setPassword(passwordEncoder.encode(password)); // 密码加密
        user.setPhone(phone);
        user.setCreditScore(80);
        user.setStatus(1);
        user.setRegisterTime(new Date());

        // ✅ 绝对写入数据库
        userRepository.save(user);

        String token = jwtUtil.generateToken(phone);
        Map<String, Object> map = new HashMap<>();
        map.put("userAccount", phone);
        map.put("token", token);

        return ResponseResult.ok("注册成功", map);
    }

    @Override
    public ResponseResult resetPassword(String account, String code) {
        Optional<User> userOptional = userRepository.findByUserAccountOrPhone(account, account);
        if (userOptional.isEmpty()) {
            return ResponseResult.fail("账号不存在");
        }
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);
        return ResponseResult.ok("密码已重置为：" + DEFAULT_PASSWORD);
    }
}