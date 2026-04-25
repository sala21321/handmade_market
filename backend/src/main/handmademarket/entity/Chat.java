package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Integer msgId;

    @Column(name = "from_user_id")
    private Integer fromUserId;

    @Column(name = "to_user_id")
    private Integer toUserId;

    @Column(name = "goods_id")
    private Integer goodsId;

    @Column(name = "custom_id")
    private Integer customId;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "image")
    private String image;

    @Column(name = "is_recall")
    private Integer isRecall;

    @Column(name = "send_time")
    private LocalDateTime sendTime;

    /** 消息类型：0-普通文字 1-图片 2-买家出价 3-卖家还价 4-接受报价 5-拒绝报价 */
    @Column(name = "msg_type")
    private Integer msgType;

    /** 议价消息中的出价金额 */
    @Column(name = "bargain_price")
    private BigDecimal bargainPrice;

    // Getters and Setters
    public Integer getMsgId() { return msgId; }
    public void setMsgId(Integer msgId) { this.msgId = msgId; }

    public Integer getFromUserId() { return fromUserId; }
    public void setFromUserId(Integer fromUserId) { this.fromUserId = fromUserId; }

    public Integer getToUserId() { return toUserId; }
    public void setToUserId(Integer toUserId) { this.toUserId = toUserId; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public Integer getCustomId() { return customId; }
    public void setCustomId(Integer customId) { this.customId = customId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getIsRecall() { return isRecall; }
    public void setIsRecall(Integer isRecall) { this.isRecall = isRecall; }

    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }

    public Integer getMsgType() { return msgType; }
    public void setMsgType(Integer msgType) { this.msgType = msgType; }

    public BigDecimal getBargainPrice() { return bargainPrice; }
    public void setBargainPrice(BigDecimal bargainPrice) { this.bargainPrice = bargainPrice; }
}