package com.example.handmademarket.controller;

import com.example.handmademarket.dto.CreateOrderRequest;
import com.example.handmademarket.dto.EvaluationRequest;
import com.example.handmademarket.service.OrderService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    // 从 Authorization header 中提取用户名；缺失或无效时直接拒绝，避免串号
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

    /** 买家：获取我的订单列表（支持状态筛选） */
    @GetMapping("/user/orders")
    public ResponseEntity<ResponseResult> getBuyerOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer status) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getBuyerOrders(username, status));
    }

    /** 卖家：获取销售订单列表（支持状态筛选） */
    @GetMapping("/seller/orders")
    public ResponseEntity<ResponseResult> getSellerOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer status) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getSellerOrders(username, status));
    }

    /** 卖家：获取销售统计 */
    @GetMapping("/seller/stats")
    public ResponseEntity<ResponseResult> getSellerStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getSellerStats(username));
    }

    /** 统一交易管理：获取用户所有订单（买家+卖家，支持按角色和状态筛选） */
    @GetMapping("/user/all-orders")
    public ResponseEntity<ResponseResult> getAllUserOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getAllUserOrders(username, status, role));
    }

    /** 统一交易管理：获取交易统计（买+卖） */
    @GetMapping("/user/trade-stats")
    public ResponseEntity<ResponseResult> getTradeStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getTradeStats(username));
    }

    /** 管理员：获取所有订单 */
    @GetMapping("/admin/orders")
    public ResponseEntity<ResponseResult> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /** 管理员：按条件搜索订单 */
    @GetMapping("/admin/orders/search")
    public ResponseEntity<ResponseResult> searchOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer buyerId,
            @RequestParam(required = false) Integer sellerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer orderType) {
        return ResponseEntity.ok(orderService.searchOrders(orderNo, buyerId, sellerId,
                status, startTime, endTime, orderType));
    }

    /** 管理员：处理订单纠纷 */
    @PutMapping("/admin/orders/{orderId}/dispute")
    public ResponseEntity<ResponseResult> handleDispute(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String disputeResult = body != null ? body.get("disputeResult") : null;
        return ResponseEntity.ok(orderService.handleDispute(orderId, disputeResult));
    }

    /** 管理员：导出订单Excel */
    @GetMapping("/admin/orders/export")
    public void exportOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer orderType,
            HttpServletResponse response) {
        orderService.exportOrders(status, startTime, endTime, orderType, response);
    }

    /** 管理员：订单统计概览 */
    @GetMapping("/admin/orders/stats")
    public ResponseEntity<ResponseResult> getAdminOrderStats() {
        return ResponseEntity.ok(orderService.getAdminOrderStats());
    }

    /** 创建订单（从购物车结算） */
    @PostMapping("/orders")
    public ResponseEntity<ResponseResult> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateOrderRequest request) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.createOrder(username, request));
    }

    /** 订单详情 */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ResponseResult> getOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getOrderDetail(username, orderId));
    }

    /** 支付订单（模拟支付） */
    @PutMapping("/orders/{orderId}/pay")
    public ResponseEntity<ResponseResult> payOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String payType = body != null ? body.get("payType") : null;
        return ResponseEntity.ok(orderService.payOrder(username, orderId, payType));
    }

    /** 支付定制订单尾款 */
    @PutMapping("/orders/{orderId}/pay-balance")
    public ResponseEntity<ResponseResult> payBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String payType = body != null ? body.get("payType") : null;
        return ResponseEntity.ok(orderService.payBalance(username, orderId, payType));
    }

    /** 创作者申请尾款支付 */
    @PutMapping("/orders/{orderId}/request-balance")
    public ResponseEntity<ResponseResult> requestBalancePayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.requestBalancePayment(username, orderId));
    }

    /** 卖家发货 */
    @PutMapping("/orders/{orderId}/ship")
    public ResponseEntity<ResponseResult> shipOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String logisticsInfo = body != null ? body.get("logisticsInfo") : null;
        return ResponseEntity.ok(orderService.shipOrder(username, orderId, logisticsInfo));
    }

    /** 买家确认收货 */
    @PutMapping("/orders/{orderId}/confirm")
    public ResponseEntity<ResponseResult> confirmReceipt(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.confirmReceipt(username, orderId));
    }

    /** 取消订单 */
    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<ResponseResult> cancelOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String cancelReason = body != null ? body.get("cancelReason") : null;
        return ResponseEntity.ok(orderService.cancelOrder(username, orderId, cancelReason));
    }

    /** 评价订单 */
    @PostMapping("/orders/{orderId}/evaluate")
    public ResponseEntity<ResponseResult> evaluateOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody EvaluationRequest request) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.evaluateOrder(username, orderId, request));
    }

    /** 重新下单（基于已取消订单） */
    @PostMapping("/orders/{orderId}/reorder")
    public ResponseEntity<ResponseResult> reorder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.reorder(username, orderId));
    }

    /** 提醒买家支付 */
    @PutMapping("/orders/{orderId}/remind-payment")
    public ResponseEntity<ResponseResult> remindPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.remindPayment(username, orderId));
    }

    /** 上传定制订单实物图 */
    @PutMapping("/orders/{orderId}/upload-image")
    public ResponseEntity<ResponseResult> uploadProductImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String username = extractUsername(authHeader);
        String imageUrl = body != null ? body.get("imageUrl") : null;
        return ResponseEntity.ok(orderService.uploadProductImage(username, orderId, imageUrl));
    }

    /** 买家申请维权 */
    @PutMapping("/orders/{orderId}/dispute")
    public ResponseEntity<ResponseResult> applyDispute(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(orderService.applyDispute(username, orderId, reason));
    }
}
