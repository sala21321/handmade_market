package com.example.handmademarket.repository;

import com.example.handmademarket.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {

    /** 获取两人之间关于某商品的聊天记录（按时间正序） */
    @Query("SELECT c FROM Chat c WHERE c.goodsId = :goodsId " +
            "AND ((c.fromUserId = :userA AND c.toUserId = :userB) " +
            "  OR (c.fromUserId = :userB AND c.toUserId = :userA)) " +
            "ORDER BY c.sendTime ASC")
    List<Chat> findConversation(@Param("goodsId") Integer goodsId,
                                @Param("userA") Integer userA,
                                @Param("userB") Integer userB);

    /** 获取两人之间关于某定制需求的聊天记录 */
    @Query("SELECT c FROM Chat c WHERE c.customId = :customId " +
            "AND ((c.fromUserId = :userA AND c.toUserId = :userB) " +
            "  OR (c.fromUserId = :userB AND c.toUserId = :userA)) " +
            "ORDER BY c.sendTime ASC")
    List<Chat> findCustomConversation(@Param("customId") Integer customId,
                                      @Param("userA") Integer userA,
                                      @Param("userB") Integer userB);

    /** 获取用户的所有会话列表（按最新消息时间倒序）：取每个对话对象的最新一条 */
    @Query("SELECT c FROM Chat c WHERE c.msgId IN " +
            "(SELECT MAX(c2.msgId) FROM Chat c2 " +
            " WHERE c2.fromUserId = :userId OR c2.toUserId = :userId " +
            " GROUP BY CASE WHEN c2.fromUserId = :userId THEN c2.toUserId ELSE c2.fromUserId END, c2.goodsId) " +
            "ORDER BY c.sendTime DESC")
    List<Chat> findUserConversations(@Param("userId") Integer userId);

    /** 查找某商品上两人之间最近的一条议价消息（类型2/3/4/5） */
    @Query("SELECT c FROM Chat c WHERE c.goodsId = :goodsId " +
            "AND c.msgType IN (2, 3, 4, 5) " +
            "AND ((c.fromUserId = :userA AND c.toUserId = :userB) " +
            "  OR (c.fromUserId = :userB AND c.toUserId = :userA)) " +
            "ORDER BY c.sendTime DESC LIMIT 1")
    Optional<Chat> findLatestBargainMsg(@Param("goodsId") Integer goodsId,
                                        @Param("userA") Integer userA,
                                        @Param("userB") Integer userB);
}
