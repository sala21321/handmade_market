package com.example.handmademarket.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.handmademarket.dto.UserUpdateRequest;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.UserService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // 获取当前登录用户ID
    private Integer getCurrentUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            String userAccount = jwtUtil.getUsernameFromToken(token);

            User user = userRepository.findByUserAccount(userAccount)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            return user.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    // 获取个人信息
    @Override
    public ResponseResult getProfile() {
        Integer userId = getCurrentUserId();

        if (userId == null) {
            return ResponseResult.fail("请先登录");
        }

        User user = userRepository.findByUserId(userId).orElse(null);

        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }

        user.setPassword(null);
        return ResponseResult.ok(user);
    }

    //校验密码是否合理
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 20) {
            return false;
        }
        if (password.contains(" ")) {
            return false;
        }
        boolean hasDigit = false;
        boolean hasLetter = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isLetter(c)) {
                hasLetter = true;
            }
        }

        return hasDigit && hasLetter;
    }

    // 修改个人信息 + 密码
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateProfile(UserUpdateRequest request) {
        Integer userId = getCurrentUserId();

        if (userId == null) {
            return ResponseResult.fail("请先登录");
        }

        User user = userRepository.findByUserId(userId).orElse(null);

        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }

        if (request.getUserAccount() != null) {
            user.setUserAccount(request.getUserAccount());
        }

        if (request.getUserName() != null) {
            user.setUserName(request.getUserName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getSpecialty() != null) {
            user.setSpecialty(request.getSpecialty());
        }

        String oldPwd = request.getOldPassword();
        String newPwd = request.getNewPassword();
        //验证
        if (oldPwd != null && newPwd != null && !oldPwd.isEmpty() && !newPwd.isEmpty()) {
            if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
                return ResponseResult.fail("原密码错误");
            }
            if (passwordEncoder.matches(newPwd, user.getPassword())) {
                return ResponseResult.fail("新密码不能与原密码相同");
            }
            if (!isValidPassword(newPwd)) {
                return ResponseResult.fail("密码必须6-20位，同时包含字母和数字，不能有空格");
            }
            user.setPassword(passwordEncoder.encode(newPwd));
        }

        userRepository.save(user);
        return ResponseResult.ok("修改成功");
    }
}