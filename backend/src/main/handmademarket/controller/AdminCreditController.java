package com.example.handmademarket.controller;

import com.example.handmademarket.entity.UserCredit;
import com.example.handmademarket.service.CreditManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/credit")
public class AdminCreditController {

    @Autowired
    private CreditManageService creditManageService;

    // 1. 管理员：获取全部用户信用列表
    @GetMapping("/list")
    public Map<String, Object> getCreditList() {
        Map<String, Object> result = new HashMap<>();
        List<UserCredit> list = creditManageService.getAllUserCredit();
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        return result;
    }

    // 2. 管理员：手动调整用户信用分数
    @PostMapping("/adjust")
    public Map<String, Object> adjustCredit(@RequestParam Long userId,
                                            @RequestParam Long adminId,
                                            @RequestParam Integer scoreChange,
                                            @RequestParam String reason) {
        Map<String, Object> result = new HashMap<>();
        try {
            creditManageService.adjustUserCredit(userId, adminId, scoreChange, reason);
            result.put("code", 200);
            result.put("msg", "信用调整成功，已自动留存操作日志");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }
}
