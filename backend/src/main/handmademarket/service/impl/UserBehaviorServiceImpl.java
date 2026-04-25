// NEW_FILE: d:\Liulanqi\handmade_market-main\backend\src\main\java\com\example\handmademarket\service\impl\UserBehaviorServiceImpl.java
package com.example.handmademarket.service.impl;

import com.example.handmademarket.entity.UserBehavior;
import com.example.handmademarket.repository.UserBehaviorRepository;
import com.example.handmademarket.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    @Override
    public UserBehavior saveBehavior(Long userId, Long goodsId, String behaviorType) {
        UserBehavior behavior = new UserBehavior(userId, goodsId, behaviorType);
        return userBehaviorRepository.save(behavior);
    }

    @Override
    public UserBehavior saveBehaviorWithExtraData(Long userId, Long goodsId, String behaviorType, String extraData) {
        UserBehavior behavior = new UserBehavior(userId, goodsId, behaviorType);
        behavior.setExtraData(extraData);
        return userBehaviorRepository.save(behavior);
    }

    @Override
    public List<UserBehavior> getUserBehaviors(Long userId) {
        return userBehaviorRepository.findByUserIdOrderByBehaviorTimeDesc(userId);
    }

    @Override
    public List<UserBehavior> getUserBehaviorsByType(Long userId, String behaviorType) {
        return userBehaviorRepository.findByUserIdAndBehaviorType(userId, behaviorType);
    }

    @Override
    public List<UserBehavior> getUserViewBehaviors(Long userId) {
        return userBehaviorRepository.findViewBehaviorsByUserIdOrderByTimeDesc(userId);
    }

    @Override
    public List<UserBehavior> getUserFavoriteBehaviors(Long userId) {
        return userBehaviorRepository.findFavoriteBehaviorsByUserIdOrderByTimeDesc(userId);
    }

    @Override
    public List<UserBehavior> getUserPurchaseBehaviors(Long userId) {
        return userBehaviorRepository.findPurchaseBehaviorsByUserIdOrderByTimeDesc(userId);
    }

    @Override
    public boolean hasUserBehavior(Long userId, Long goodsId, String behaviorType) {
        UserBehavior behavior = userBehaviorRepository.findByUserIdAndGoodsIdAndBehaviorType(userId, goodsId, behaviorType);
        return behavior != null;
    }

    @Override
    public List<Long> getUserInteractedGoodsIds(Long userId) {
        return userBehaviorRepository.findDistinctGoodsIdsByUserIdAndBehaviorTypes(userId);
    }

    @Override
    public void deleteBehaviorById(Long behaviorId) {
        userBehaviorRepository.deleteById(behaviorId);
    }

    @Override
    public void deleteBehaviorsByUserId(Long userId) {
        List<UserBehavior> behaviors = getUserBehaviors(userId);
        for (UserBehavior behavior : behaviors) {
            userBehaviorRepository.deleteById(behavior.getBehaviorId());
        }
    }
}