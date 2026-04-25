package com.example.handmademarket.service.impl;

import com.example.handmademarket.entity.Chat;
import com.example.handmademarket.entity.Goods;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.ChatRepository;
import com.example.handmademarket.repository.GoodsRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.ChatService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final GoodsRepository goodsRepository;

    public ChatServiceImpl(ChatRepository chatRepository,
                           UserRepository userRepository,
                           GoodsRepository goodsRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.goodsRepository = goodsRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUserAccount(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private String msgTypeToString(Integer type) {
        if (type == null) return "text";
        return switch (type) {
            case 0 -> "text";
            case 1 -> "image";
            case 2 -> "buyer_offer";
            case 3 -> "seller_counter";
            case 4 -> "accept";
            case 5 -> "reject";
            default -> "text";
        };
    }

    private Map<String, Object> buildChatMap(Chat msg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("msgId", msg.getMsgId());
        map.put("fromUserId", msg.getFromUserId());
        map.put("toUserId", msg.getToUserId());
        map.put("goodsId", msg.getGoodsId());
        map.put("customId", msg.getCustomId());
        map.put("content", msg.getIsRecall() != null && msg.getIsRecall() == 1 ? "[消息已撤回]" : msg.getContent());
        map.put("image", msg.getIsRecall() != null && msg.getIsRecall() == 1 ? null : msg.getImage());
        map.put("msgType", msgTypeToString(msg.getMsgType()));
        map.put("bargainPrice", msg.getBargainPrice());
        map.put("isRecall", msg.getIsRecall() != null && msg.getIsRecall() == 1);
        map.put("sendTime", msg.getSendTime() != null
                ? msg.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        // 发送者信息
        if (msg.getFromUserId() != null) {
            userRepository.findById(msg.getFromUserId().longValue())
                    .ifPresent(u -> {
                        map.put("fromUserName", u.getUserName() != null ? u.getUserName() : u.getUserAccount());
                        map.put("fromAvatar", u.getAvatar());
                    });
        }
        return map;
    }

    // ==================== 在线沟通 ====================

    @Override
    @Transactional
    public ResponseResult sendMessage(String username, Integer toUserId, Integer goodsId,
                                       Integer customId, String content) {
        User user = getUserByUsername(username);
        if (content == null || content.isBlank()) {
            return ResponseResult.fail("消息内容不能为空");
        }
        if (toUserId == null) {
            return ResponseResult.fail("接收人不能为空");
        }
        if (user.getUserId().intValue() == toUserId) {
            return ResponseResult.fail("不能给自己发消息");
        }

        Chat chat = new Chat();
        chat.setFromUserId(user.getUserId().intValue());
        chat.setToUserId(toUserId);
        chat.setGoodsId(goodsId);
        chat.setCustomId(customId);
        chat.setContent(content);
        chat.setMsgType(0); // 普通文字
        chat.setIsRecall(0);
        chat.setSendTime(LocalDateTime.now());
        chatRepository.save(chat);

        return ResponseResult.ok("发送成功", buildChatMap(chat));
    }

    @Override
    @Transactional
    public ResponseResult sendImage(String username, Integer toUserId, Integer goodsId,
                                     Integer customId, String imageUrl) {
        User user = getUserByUsername(username);
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseResult.fail("图片链接不能为空");
        }
        if (toUserId == null) {
            return ResponseResult.fail("接收人不能为空");
        }

        Chat chat = new Chat();
        chat.setFromUserId(user.getUserId().intValue());
        chat.setToUserId(toUserId);
        chat.setGoodsId(goodsId);
        chat.setCustomId(customId);
        chat.setImage(imageUrl);
        chat.setContent("[图片]");
        chat.setMsgType(1); // 图片
        chat.setIsRecall(0);
        chat.setSendTime(LocalDateTime.now());
        chatRepository.save(chat);

        return ResponseResult.ok("发送成功", buildChatMap(chat));
    }

    @Override
    @Transactional
    public ResponseResult recallMessage(String username, Integer msgId) {
        User user = getUserByUsername(username);
        Chat chat = chatRepository.findById(msgId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        if (!chat.getFromUserId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("只能撤回自己发送的消息");
        }

        // 2分钟内可撤回
        if (chat.getSendTime() != null &&
                chat.getSendTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            return ResponseResult.fail("只能撤回2分钟内的消息");
        }

        chat.setIsRecall(1);
        chatRepository.save(chat);
        return ResponseResult.ok("消息已撤回");
    }

    @Override
    public ResponseResult getConversation(String username, Integer otherUserId, Integer goodsId) {
        User user = getUserByUsername(username);
        List<Chat> messages = chatRepository.findConversation(
                goodsId, user.getUserId().intValue(), otherUserId);
        List<Map<String, Object>> result = messages.stream()
                .map(this::buildChatMap).collect(Collectors.toList());

        // 附带商品信息
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("messages", result);
        if (goodsId != null) {
            goodsRepository.findById(goodsId.longValue()).ifPresent(goods -> {
                Map<String, Object> goodsInfo = new LinkedHashMap<>();
                goodsInfo.put("goodsId", goods.getId());
                goodsInfo.put("name", goods.getTitle());
                goodsInfo.put("price", goods.getPrice());
                goodsInfo.put("image", goods.getImageUrl());
                data.put("goods", goodsInfo);
            });
        }
        // 对方信息
        userRepository.findById(otherUserId.longValue()).ifPresent(u -> {
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("userId", u.getUserId());
            userInfo.put("userName", u.getUserName() != null ? u.getUserName() : u.getUserAccount());
            userInfo.put("avatar", u.getAvatar());
            data.put("otherUser", userInfo);
        });

        return ResponseResult.ok(data);
    }

    @Override
    public ResponseResult getCustomConversation(String username, Integer otherUserId, Integer customId) {
        User user = getUserByUsername(username);
        List<Chat> messages = chatRepository.findCustomConversation(
                customId, user.getUserId().intValue(), otherUserId);
        List<Map<String, Object>> result = messages.stream()
                .map(this::buildChatMap).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("messages", result);
        return ResponseResult.ok(data);
    }

    @Override
    public ResponseResult getConversationList(String username) {
        User user = getUserByUsername(username);
        List<Chat> latestMessages = chatRepository.findUserConversations(user.getUserId().intValue());

        List<Map<String, Object>> result = latestMessages.stream().map(msg -> {
            Map<String, Object> map = buildChatMap(msg);

            // 确定对方用户ID
            Integer otherUserId = msg.getFromUserId().equals(user.getUserId().intValue())
                    ? msg.getToUserId() : msg.getFromUserId();
            map.put("otherUserId", otherUserId);

            // 对方信息
            userRepository.findById(otherUserId.longValue()).ifPresent(u -> {
                map.put("otherUserName", u.getUserName() != null ? u.getUserName() : u.getUserAccount());
                map.put("otherAvatar", u.getAvatar());
            });

            // 商品信息
            if (msg.getGoodsId() != null) {
                goodsRepository.findById(msg.getGoodsId().longValue()).ifPresent(goods -> {
                    map.put("goodsName", goods.getTitle());
                    map.put("goodsImage", goods.getImageUrl());
                    map.put("goodsPrice", goods.getPrice());
                });
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    // ==================== 议价功能 ====================

    @Override
    @Transactional
    public ResponseResult makeOffer(String username, Integer goodsId, Integer sellerId, BigDecimal offerPrice) {
        User buyer = getUserByUsername(username);
        if (goodsId == null || sellerId == null) {
            return ResponseResult.fail("商品ID和卖家ID不能为空");
        }
        if (offerPrice == null || offerPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseResult.fail("出价必须大于0");
        }

        Goods goods = goodsRepository.findById(goodsId.longValue())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 不能对自己发布的商品出价
        if (goods.getCreatorId() != null && goods.getCreatorId().equals(buyer.getUserId().intValue())) {
            return ResponseResult.fail("不能对自己的商品出价");
        }

        // 检查是否低于底价（不透露底价具体数值）
        if (goods.getReservePrice() != null && offerPrice.compareTo(BigDecimal.valueOf(goods.getReservePrice())) < 0) {
            return ResponseResult.fail("出价过低，请适当提高");
        }

        // 如果出价 >= 售价，直接按售价成交
        boolean directDeal = goods.getPrice() != null && offerPrice.compareTo(BigDecimal.valueOf(goods.getPrice())) >= 0;

        Chat chat = new Chat();
        chat.setFromUserId(buyer.getUserId().intValue());
        chat.setToUserId(sellerId);
        chat.setGoodsId(goodsId);
        chat.setMsgType(2); // buyer_offer
        chat.setBargainPrice(offerPrice);
        chat.setContent("买家出价：¥" + offerPrice.toPlainString());
        chat.setIsRecall(0);
        chat.setSendTime(LocalDateTime.now());
        chatRepository.save(chat);

        if (directDeal) {
            // 自动接受
            Chat acceptMsg = new Chat();
            acceptMsg.setFromUserId(sellerId);
            acceptMsg.setToUserId(buyer.getUserId().intValue());
            acceptMsg.setGoodsId(goodsId);
            acceptMsg.setMsgType(4); // accept
            acceptMsg.setBargainPrice(offerPrice);
            acceptMsg.setContent("出价已达到售价，自动成交：¥" + offerPrice.toPlainString());
            acceptMsg.setIsRecall(0);
            acceptMsg.setSendTime(LocalDateTime.now());
            chatRepository.save(acceptMsg);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "accepted");
            data.put("dealPrice", offerPrice);
            data.put("goodsId", goodsId);
            data.put("message", "出价已达到售价，自动成交！请下单");
            return ResponseResult.ok("议价成功", data);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "pending");
        data.put("offerPrice", offerPrice);
        data.put("goodsId", goodsId);
        data.put("message", "出价已发送，等待卖家回复");
        return ResponseResult.ok("出价成功", data);
    }

    @Override
    @Transactional
    public ResponseResult counterOffer(String username, Integer goodsId, Integer buyerId, BigDecimal counterPrice) {
        User seller = getUserByUsername(username);
        if (goodsId == null || buyerId == null) {
            return ResponseResult.fail("商品ID和买家ID不能为空");
        }
        if (counterPrice == null || counterPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseResult.fail("还价金额必须大于0");
        }

        Goods goods = goodsRepository.findById(goodsId.longValue())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 验证是该商品的创作者
        if (goods.getCreatorId() == null || !goods.getCreatorId().equals(seller.getUserId().intValue())) {
            return ResponseResult.fail("只有商品创作者可以还价");
        }

        // 还价不能低于底价
        if (goods.getReservePrice() != null && counterPrice.compareTo(BigDecimal.valueOf(goods.getReservePrice())) < 0) {
            return ResponseResult.fail("还价不能低于底价");
        }

        Chat chat = new Chat();
        chat.setFromUserId(seller.getUserId().intValue());
        chat.setToUserId(buyerId);
        chat.setGoodsId(goodsId);
        chat.setMsgType(3); // seller_counter
        chat.setBargainPrice(counterPrice);
        chat.setContent("卖家还价：¥" + counterPrice.toPlainString());
        chat.setIsRecall(0);
        chat.setSendTime(LocalDateTime.now());
        chatRepository.save(chat);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "counter");
        data.put("counterPrice", counterPrice);
        data.put("message", "还价已发送，等待买家回复");
        return ResponseResult.ok("还价成功", data);
    }

    @Override
    @Transactional
    public ResponseResult acceptOffer(String username, Integer goodsId, Integer otherUserId) {
        User user = getUserByUsername(username);

        // 查找最近的议价消息
        Optional<Chat> latestOpt = chatRepository.findLatestBargainMsg(
                goodsId, user.getUserId().intValue(), otherUserId);
        if (latestOpt.isEmpty()) {
            return ResponseResult.fail("没有找到待处理的议价记录");
        }

        Chat latest = latestOpt.get();
        // 不能接受自己的报价
        if (latest.getFromUserId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("不能接受自己的报价");
        }
        // 最近的消息已经是接受/拒绝则不能重复操作
        if (latest.getMsgType() == 4 || latest.getMsgType() == 5) {
            return ResponseResult.fail("该议价已结束");
        }

        BigDecimal dealPrice = latest.getBargainPrice();

        Chat acceptMsg = new Chat();
        acceptMsg.setFromUserId(user.getUserId().intValue());
        acceptMsg.setToUserId(otherUserId);
        acceptMsg.setGoodsId(goodsId);
        acceptMsg.setMsgType(4); // accept
        acceptMsg.setBargainPrice(dealPrice);
        acceptMsg.setContent("已接受报价：¥" + dealPrice.toPlainString() + "，请买家下单");
        acceptMsg.setIsRecall(0);
        acceptMsg.setSendTime(LocalDateTime.now());
        chatRepository.save(acceptMsg);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "accepted");
        data.put("dealPrice", dealPrice);
        data.put("goodsId", goodsId);
        data.put("message", "议价成功，成交价：¥" + dealPrice.toPlainString());
        return ResponseResult.ok("接受报价成功", data);
    }

    @Override
    @Transactional
    public ResponseResult rejectOffer(String username, Integer goodsId, Integer otherUserId) {
        User user = getUserByUsername(username);

        Optional<Chat> latestOpt = chatRepository.findLatestBargainMsg(
                goodsId, user.getUserId().intValue(), otherUserId);
        if (latestOpt.isEmpty()) {
            return ResponseResult.fail("没有找到待处理的议价记录");
        }

        Chat latest = latestOpt.get();
        if (latest.getFromUserId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("不能拒绝自己的报价");
        }
        if (latest.getMsgType() == 4 || latest.getMsgType() == 5) {
            return ResponseResult.fail("该议价已结束");
        }

        Chat rejectMsg = new Chat();
        rejectMsg.setFromUserId(user.getUserId().intValue());
        rejectMsg.setToUserId(otherUserId);
        rejectMsg.setGoodsId(goodsId);
        rejectMsg.setMsgType(5); // reject
        rejectMsg.setBargainPrice(latest.getBargainPrice());
        rejectMsg.setContent("已拒绝报价：¥" + latest.getBargainPrice().toPlainString());
        rejectMsg.setIsRecall(0);
        rejectMsg.setSendTime(LocalDateTime.now());
        chatRepository.save(rejectMsg);

        return ResponseResult.ok("已拒绝报价");
    }

    @Override
    public ResponseResult getBargainHistory(String username, Integer goodsId, Integer otherUserId) {
        User user = getUserByUsername(username);
        List<Chat> messages = chatRepository.findConversation(
                goodsId, user.getUserId().intValue(), otherUserId);

        // 只取议价相关消息
        List<Map<String, Object>> result = messages.stream()
                .filter(m -> m.getMsgType() != null && m.getMsgType() >= 2)
                .map(this::buildChatMap)
                .collect(Collectors.toList());

        // 附带当前议价状态
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", result);

        // 判断议价状态
        Optional<Chat> latestOpt = chatRepository.findLatestBargainMsg(
                goodsId, user.getUserId().intValue(), otherUserId);
        if (latestOpt.isPresent()) {
            Chat latest = latestOpt.get();
            String bargainStatus = switch (latest.getMsgType()) {
                case 2, 3 -> "negotiating"; // 议价进行中
                case 4 -> "accepted";       // 已成交
                case 5 -> "rejected";       // 已拒绝
                default -> "none";
            };
            data.put("bargainStatus", bargainStatus);
            data.put("latestPrice", latest.getBargainPrice());
        } else {
            data.put("bargainStatus", "none");
        }

        // 商品价格信息
        goodsRepository.findById(goodsId.longValue()).ifPresent(goods -> {
            data.put("originalPrice", goods.getPrice());
            // 不暴露底价给买家（只有卖家能看到）
            if (goods.getCreatorId() != null && goods.getCreatorId().equals(user.getUserId().intValue())) {
                data.put("reservePrice", goods.getReservePrice());
            }
        });

        return ResponseResult.ok(data);
    }
}
