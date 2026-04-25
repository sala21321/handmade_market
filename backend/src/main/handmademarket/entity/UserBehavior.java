// NEW_FILE: d:\Liulanqi\handmade_market-main\backend\src\main\java\com\example\handmademarket\entity\UserBehavior.java
package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user_behavior")
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "behavior_id")
    private Long behaviorId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "goods_id")
    private Long goodsId;

    @Column(name = "behavior_type")
    private String behaviorType; // VIEW, FAVORITE, PURCHASE, SEARCH等

    @Column(name = "behavior_time")
    private LocalDateTime behaviorTime;

    @Column(name = "extra_data")
    private String extraData; // 存储额外的数据，如搜索关键词等

    // 构造函数
    public UserBehavior() {
        this.behaviorTime = LocalDateTime.now();
    }

    public UserBehavior(Long userId, Long goodsId, String behaviorType) {
        this.userId = userId;
        this.goodsId = goodsId;
        this.behaviorType = behaviorType;
        this.behaviorTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getBehaviorId() {
        return behaviorId;
    }

    public void setBehaviorId(Long behaviorId) {
        this.behaviorId = behaviorId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(String behaviorType) {
        this.behaviorType = behaviorType;
    }

    public LocalDateTime getBehaviorTime() {
        return behaviorTime;
    }

    public void setBehaviorTime(LocalDateTime behaviorTime) {
        this.behaviorTime = behaviorTime;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}