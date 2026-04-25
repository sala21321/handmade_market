// NEW_FILE: d:\Liulanqi\handmade_market-main\backend\src\main\java\com\example\handmademarket\service\UserBehaviorService.java
package com.example.handmademarket.service;

import com.example.handmademarket.entity.UserBehavior;

import java.util.List;

public interface UserBehaviorService {
    UserBehavior saveBehavior(Long userId, Long goodsId, String behaviorType);

    UserBehavior saveBehaviorWithExtraData(Long userId, Long goodsId, String behaviorType, String extraData);

    List<UserBehavior> getUserBehaviors(Long userId);

    List<UserBehavior> getUserBehaviorsByType(Long userId, String behaviorType);

    List<UserBehavior> getUserViewBehaviors(Long userId);

    List<UserBehavior> getUserFavoriteBehaviors(Long userId);

    List<UserBehavior> getUserPurchaseBehaviors(Long userId);

    boolean hasUserBehavior(Long userId, Long goodsId, String behaviorType);

    List<Long> getUserInteractedGoodsIds(Long userId);

    void deleteBehaviorById(Long behaviorId);

    void deleteBehaviorsByUserId(Long userId);
}