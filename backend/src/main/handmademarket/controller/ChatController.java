package com.example.handmademarket.controller;

import com.example.handmademarket.service.ChatService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    public ChatController(ChatService chatService, JwtUtil jwtUtil) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    private String extractUsername(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return "testuser";
        if (!authHeader.startsWith("Bearer ")) return "testuser";
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return "testuser";
        return jwtUtil.getUsernameFromToken(token);
    }

    // ==================== 在线沟通 ====================

    /** 发送文字消息 */
    @PostMapping("/send")
    public ResponseEntity<ResponseResult> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        Integer toUserId = body.get("toUserId") != null ? Integer.valueOf(body.get("toUserId").toString()) : null;
        Integer goodsId = body.get("goodsId") != null ? Integer.valueOf(body.get("goodsId").toString()) : null;
        Integer customId = body.get("customId") != null ? Integer.valueOf(body.get("customId").toString()) : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        return ResponseEntity.ok(chatService.sendMessage(username, toUserId, goodsId, customId, content));
    }

    /** 发送图片消息 */
    @PostMapping("/send-image")
    public ResponseEntity<ResponseResult> sendImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        Integer toUserId = body.get("toUserId") != null ? Integer.valueOf(body.get("toUserId").toString()) : null;
        Integer goodsId = body.get("goodsId") != null ? Integer.valueOf(body.get("goodsId").toString()) : null;
        Integer customId = body.get("customId") != null ? Integer.valueOf(body.get("customId").toString()) : null;
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        return ResponseEntity.ok(chatService.sendImage(username, toUserId, goodsId, customId, imageUrl));
    }

    /** 撤回消息 */
    @PutMapping("/recall/{msgId}")
    public ResponseEntity<ResponseResult> recallMessage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer msgId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(chatService.recallMessage(username, msgId));
    }

    /** 获取与某人关于某商品的聊天记录 */
    @GetMapping("/conversation")
    public ResponseEntity<ResponseResult> getConversation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Integer otherUserId,
            @RequestParam Integer goodsId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(chatService.getConversation(username, otherUserId, goodsId));
    }

    /** 获取与某人关于某定制需求的聊天记录 */
    @GetMapping("/conversation/custom")
    public ResponseEntity<ResponseResult> getCustomConversation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Integer otherUserId,
            @RequestParam Integer customId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(chatService.getCustomConversation(username, otherUserId, customId));
    }

    /** 获取用户的会话列表 */
    @GetMapping("/conversations")
    public ResponseEntity<ResponseResult> getConversationList(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(chatService.getConversationList(username));
    }

    // ==================== 议价功能 ====================

    /** 买家：对商品出价 */
    @PostMapping("/bargain/offer")
    public ResponseEntity<ResponseResult> makeOffer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        Integer goodsId = body.get("goodsId") != null ? Integer.valueOf(body.get("goodsId").toString()) : null;
        Integer sellerId = body.get("sellerId") != null ? Integer.valueOf(body.get("sellerId").toString()) : null;
        BigDecimal offerPrice = body.get("offerPrice") != null ? new BigDecimal(body.get("offerPrice").toString()) : null;
        return ResponseEntity.ok(chatService.makeOffer(username, goodsId, sellerId, offerPrice));
    }

    /** 卖家：还价 */
    @PostMapping("/bargain/counter")
    public ResponseEntity<ResponseResult> counterOffer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        Integer goodsId = body.get("goodsId") != null ? Integer.valueOf(body.get("goodsId").toString()) : null;
        Integer buyerId = body.get("buyerId") != null ? Integer.valueOf(body.get("buyerId").toString()) : null;
        BigDecimal counterPrice = body.get("counterPrice") != null ? new BigDecimal(body.get("counterPrice").toString()) : null;
        return ResponseEntity.ok(chatService.counterOffer(username, goodsId, buyerId, counterPrice));
    }

    /** 接受对方报价 */
    @PostMapping("/bargain/accept")
    public ResponseEntity<ResponseResult> acceptOffer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        Integer goodsId = body.get("goodsId") != null ? Integer.valueOf(body.get("goodsId").toString()) : null;
        Integer otherUserId = body.get("otherUserId") != null ? Integer.valueOf(body.get("otherUserId").toString()) : null;
        return ResponseEntity.ok(chatService.acceptOffer(username, goodsId, otherUserId));
    }

    /** 拒绝对方报价 */
    @PostMapping("/bargain/reject")
    public ResponseEntity<ResponseResult> rejectOffer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        String username = extractUsername(authHeader);
        Integer goodsId = body.get("goodsId") != null ? Integer.valueOf(body.get("goodsId").toString()) : null;
        Integer otherUserId = body.get("otherUserId") != null ? Integer.valueOf(body.get("otherUserId").toString()) : null;
        return ResponseEntity.ok(chatService.rejectOffer(username, goodsId, otherUserId));
    }

    /** 获取议价记录 */
    @GetMapping("/bargain/history")
    public ResponseEntity<ResponseResult> getBargainHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Integer goodsId,
            @RequestParam Integer otherUserId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(chatService.getBargainHistory(username, goodsId, otherUserId));
    }
}
