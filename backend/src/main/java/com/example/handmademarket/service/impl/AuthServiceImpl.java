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

    public AuthServiceImpl(UserRepository userRepository, AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 生成唯一账号
    private String generateUniqueUserAccount() {
        String userAccount;
        do {
            userAccount = "user" + System.currentTimeMillis();
        } while (userRepository.existsByUserAccount(userAccount));
        return userAccount;
    }

    @Override
    public ResponseResult login(LoginRequest request) {
        if (request.getUserAccount() == null || request.getPassword() == null) {
            return ResponseResult.fail("账号密码不能为空");
        }

        // 登录类型 1=用户 2=管理员
        if (1 == request.getType()) {
            return userLogin(request);
        } else if (2 == request.getType()) {
            return adminLogin(request);
        }
        return ResponseResult.fail("登录类型错误");
    }

    //用户登录
    private ResponseResult userLogin(LoginRequest request) {
        // 根据 手机号 或 账号 查询
        Optional<User> userOptional = userRepository.findByUserAccountOrPhone(
        request.getUserAccount(), 
        request.getUserAccount()
        );
        if (userOptional.isEmpty()) {
            return ResponseResult.fail("账号或密码错误");
        }
        User user = userOptional.get();
        // 判断是否锁定
        if (user.getStatus() == 2) {
            long now = new Date().getTime();
            long lockTime = user.getLockTime().getTime();
            if (now - lockTime < 10 * 60 * 1000) { // 10分钟
                return ResponseResult.fail("账号已锁定，请10分钟后再试");
            } else {
                // 自动解锁
                user.setStatus(1);
                user.setPwdErrorCount(0);
                user.setLockTime(null);
                userRepository.save(user);
            }
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            Integer errorCount = user.getPwdErrorCount();
            if (errorCount == null) {
                errorCount = 1;
            } else {
                errorCount++;
            }
            user.setPwdErrorCount(errorCount);

            if (errorCount >= 5) { // 5次错误锁定
                user.setStatus(2);
                user.setLockTime(new Date());
            }
            userRepository.save(user);
            return ResponseResult.fail("密码错误，剩余" + (5 - errorCount) + "次机会");
        }

        // 登录成功：重置错误次数
        user.setPwdErrorCount(0);
        user.setLastLoginTime(new Date());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUserAccount());
        Map<String, Object> map = new HashMap<>();
        map.put("userAccount", user.getUserAccount());
        map.put("token", token);
        return ResponseResult.ok("登录成功", map);
    }

    //管理员登录
    private ResponseResult adminLogin(LoginRequest request) {
        Admin admin = adminRepository.findByAdminAccount(request.getUserAccount());
        if (admin == null) {
            return ResponseResult.fail("管理员账号不存在");
        }

        if (admin.getStatus() == 0) {
            return ResponseResult.fail("账号已禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getAdminPwd())) {
            return ResponseResult.fail("密码错误");
        }

        String token = jwtUtil.generateToken(admin.getAdminAccount());
        Map<String, Object> map = new HashMap<>();
        map.put("userAccount", admin.getAdminAccount());
        map.put("token", token);
        return ResponseResult.ok("登录成功", map);
    }

    @Override
    public ResponseResult register(RegisterRequest request) {

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return ResponseResult.fail("电话号码不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseResult.fail("密码不能为空");
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return ResponseResult.fail("确认密码不能为空");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseResult.fail("两次输入的密码不一致");
        }

        if (request.getPhone().length() != 11) {
            return ResponseResult.fail("手机号格式不正确");
        }

        //密码规则：6-20位 + 字母+数字
        String password = request.getPassword();
        if (password.length() < 6 || password.length() > 20) {
            return ResponseResult.fail("密码长度必须在6-20位之间");
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,20}$")) {
            return ResponseResult.fail("密码必须是字母+数字的组合");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            return ResponseResult.fail("该手机号已注册，请直接登录");
        }

        String userAccount = generateUniqueUserAccount();

        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "1");
        user.setCreditScore(80);
        user.setStatus(1);
        user.setRegisterTime(new Date());

        userRepository.save(user);

        String token = jwtUtil.generateToken(userAccount);

        Map<String, Object> map = new HashMap<>();
        map.put("userAccount", userAccount);
        map.put("token", token);
        return ResponseResult.ok("注册成功", map);
    }
}