package com.example.handmademarket.service;

import com.example.handmademarket.util.ResponseResult;
import java.math.BigDecimal;

public interface ChatService {

    /** 发送普通文字消息 */
    ResponseResult sendMessage(String username, Integer toUserId, Integer goodsId,
                               Integer customId, String content);

    /** 发送图片消息 */
    ResponseResult sendImage(String username, Integer toUserId, Integer goodsId,
                             Integer customId, String imageUrl);

    /** 撤回消息 */
    ResponseResult recallMessage(String username, Integer msgId);

    /** 获取与某人关于某商品的聊天记录 */
    ResponseResult getConversation(String username, Integer otherUserId, Integer goodsId);

    /** 获取与某人关于某定制需求的聊天记录 */
    ResponseResult getCustomConversation(String username, Integer otherUserId, Integer customId);

    /** 获取用户的会话列表 */
    ResponseResult getConversationList(String username);

    // ==================== 议价功能（合并在聊天中） ====================

    /** 买家：对商品出价 */
    ResponseResult makeOffer(String username, Integer goodsId, Integer sellerId, BigDecimal offerPrice);

    /** 卖家：还价 */
    ResponseResult counterOffer(String username, Integer goodsId, Integer buyerId, BigDecimal counterPrice);

    /** 接受对方报价（买家接受还价 / 卖家接受出价） */
    ResponseResult acceptOffer(String username, Integer goodsId, Integer otherUserId);

    /** 拒绝对方报价 */
    ResponseResult rejectOffer(String username, Integer goodsId, Integer otherUserId);

    /** 获取某商品的议价记录 */
    ResponseResult getBargainHistory(String username, Integer goodsId, Integer otherUserId);
}
