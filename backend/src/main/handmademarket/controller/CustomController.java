package com.example.handmademarket.controller;

import com.example.handmademarket.dto.CustomRequest;
import com.example.handmademarket.service.CustomService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CustomController {

    private final CustomService customService;
    private final JwtUtil jwtUtil;

    public CustomController(CustomService customService, JwtUtil jwtUtil) {
        this.customService = customService;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录凭证格式错误");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        return jwtUtil.getUsernameFromToken(token);
    }

    // ==================== 需求方操作 ====================

    /** 提交定制需求（需求方） */
    @PostMapping("/custom")
    public ResponseEntity<ResponseResult> submitCustom(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CustomRequest request) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(customService.submitCustom(username, request));
    }

    /** 查看我提交的定制需求（需求方） */
    @GetMapping("/custom/my")
    public ResponseEntity<ResponseResult> getMyCustoms(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(customService.getMyCustoms(username));
    }

    /** 取消定制需求（需求方） */
    @PutMapping("/custom/{customId}/cancel")
    public ResponseEntity<ResponseResult> cancelCustom(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer customId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(customService.cancelCustom(username, customId));
    }

    /** 确认定制完成 → 自动生成定制订单（需求方） */
    @PostMapping("/custom/{customId}/confirm")
    public ResponseEntity<ResponseResult> confirmCustom(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer customId,
            @RequestBody Map<String, String> body) {
        String username = extractUsername(authHeader);
        String deliveryAddress = body != null ? body.get("deliveryAddress") : null;
        return ResponseEntity.ok(customService.confirmCustom(username, customId, deliveryAddress));
    }

    // ==================== 接单方操作 ====================

    /** 查看可接的定制需求（接单方） */
    @GetMapping("/custom/available")
    public ResponseEntity<ResponseResult> getAvailableCustoms(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(customService.getAvailableCustoms(username));
    }

    /** 查看我接的定制单（接单方） */
    @GetMapping("/custom/accepted")
    public ResponseEntity<ResponseResult> getMyAcceptedCustoms(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(customService.getMyAcceptedCustoms(username));
    }

    /** 接受定制需求（接单方，含报价） */
    @PostMapping("/custom/{customId}/accept")
    public ResponseEntity<ResponseResult> acceptCustom(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer customId,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        BigDecimal quotedPrice = null;
        if (body != null && body.get("quotedPrice") != null) {
            quotedPrice = new BigDecimal(body.get("quotedPrice").toString());
        }
        return ResponseEntity.ok(customService.acceptCustom(username, customId, quotedPrice));
    }

    /** 拒绝定制需求（接单方） */
    @PostMapping("/custom/{customId}/reject")
    public ResponseEntity<ResponseResult> rejectCustom(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer customId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(customService.rejectCustom(username, customId, reason));
    }

    /** 交付定制作品（接单方） */
    @PostMapping("/custom/{customId}/deliver")
    public ResponseEntity<ResponseResult> deliverCustom(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer customId,
            @RequestBody Map<String, String> body) {
        String username = extractUsername(authHeader);
        String deliverContent = body != null ? body.get("deliverContent") : null;
        return ResponseEntity.ok(customService.deliverCustom(username, customId, deliverContent));
    }

    // ==================== 通用 ====================

    /** 查看定制需求详情 */
    @GetMapping("/custom/{customId}")
    public ResponseEntity<ResponseResult> getCustomDetail(@PathVariable Integer customId) {
        return ResponseEntity.ok(customService.getCustomDetail(customId));
    }

    /** 管理员：查看所有定制需求 */
    @GetMapping("/admin/customs")
    public ResponseEntity<ResponseResult> getAllCustoms() {
        return ResponseEntity.ok(customService.getAllCustoms());
    }
}