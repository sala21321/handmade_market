// NEW_FILE: d:\Liulanqi\handmade_market-main\backend\src\main\java\com\example\handmademarket\repository\UserBehaviorRepository.java
package com.example.handmademarket.repository;

import com.example.handmademarket.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    List<UserBehavior> findByUserId(Long userId);

    List<UserBehavior> findByUserIdAndBehaviorType(Long userId, String behaviorType);

    List<UserBehavior> findByUserIdOrderByBehaviorTimeDesc(Long userId);

    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.behaviorType = 'VIEW' ORDER BY ub.behaviorTime DESC")
    List<UserBehavior> findViewBehaviorsByUserIdOrderByTimeDesc(@Param("userId") Long userId);

    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.behaviorType = 'FAVORITE' ORDER BY ub.behaviorTime DESC")
    List<UserBehavior> findFavoriteBehaviorsByUserIdOrderByTimeDesc(@Param("userId") Long userId);

    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.behaviorType = 'PURCHASE' ORDER BY ub.behaviorTime DESC")
    List<UserBehavior> findPurchaseBehaviorsByUserIdOrderByTimeDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ub.goodsId FROM UserBehavior ub WHERE ub.userId = :userId AND ub.behaviorType IN ('VIEW', 'FAVORITE', 'PURCHASE')")
    List<Long> findDistinctGoodsIdsByUserIdAndBehaviorTypes(@Param("userId") Long userId);

    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.goodsId = :goodsId AND ub.behaviorType = :behaviorType")
    UserBehavior findByUserIdAndGoodsIdAndBehaviorType(@Param("userId") Long userId, 
                                                      @Param("goodsId") Long goodsId, 
                                                      @Param("behaviorType") String behaviorType);
}