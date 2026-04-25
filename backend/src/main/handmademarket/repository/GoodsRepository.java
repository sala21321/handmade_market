package com.example.handmademarket.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.handmademarket.entity.Goods;

import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

    Page<Goods> findByCategory(String category, Pageable pageable);

    Page<Goods> findByStatus(Integer status, Pageable pageable);

    List<Goods> findByCreatorIdOrderByPublishTimeDesc(Long creatorId);

    Page<Goods> findByCreatorIdAndStatus(Long creatorId, Integer status, Pageable pageable);

    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND (g.goodsName LIKE %:keyword% OR g.details LIKE %:keyword%)")
    Page<Goods> searchGoods(@Param("keyword") String keyword, Pageable pageable);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Goods g WHERE g.id = :id")
    Optional<Goods> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND (g.goodsName LIKE %:keyword% OR g.details LIKE %:keyword%)")
    java.util.List<Goods> searchByKeyword(@Param("keyword") String keyword);

    long countByCreatorId(Long creatorId);

    // ... existing code ...
    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND " +
           "(:keyword IS NULL OR " +
           "g.goodsName LIKE %:keyword% OR " +
           "g.material LIKE %:keyword% OR " +
           "g.style LIKE %:keyword% OR " +
           "g.details LIKE %:keyword%) AND " +
           "(:category IS NULL OR g.category = :category) AND " +
           "(:material IS NULL OR g.material = :material) AND " +
           "(:style IS NULL OR g.style = :style) AND " +
           "(:minPrice IS NULL OR g.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR g.price <= :maxPrice)")
    List<Goods> searchGoodsWithoutPaging(@Param("keyword") String keyword,
                                        @Param("category") String category,
                                        @Param("material") String material,
                                        @Param("style") String style,
                                        @Param("minPrice") Integer minPrice,
                                        @Param("maxPrice") Integer maxPrice);
// ... existing code ...
    
    // 通过创作者信用分筛选商品
    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND g.creatorId IN " +
           "(SELECT u.userId FROM User u WHERE u.creditScore >= :minCredit)")
    List<Goods> findByCreatorMinCredit(@Param("minCredit") Integer minCredit);

    // 获取热门商品（按销量排序）
    @Query(value = "SELECT g.* FROM tb_goods g " +
           "LEFT JOIN (SELECT goods_id, COUNT(*) as sales_count FROM tb_order_goods GROUP BY goods_id) oc " +
           "ON g.goods_id = oc.goods_id " +
           "WHERE g.status = 1 " +
           "ORDER BY COALESCE(oc.sales_count, 0) DESC, g.publish_time DESC LIMIT :limit", 
           nativeQuery = true)
    List<Goods> findTopSellingGoods(@Param("limit") int limit);
    
    // 获取新上架商品
    @Query("SELECT g FROM Goods g WHERE g.status = 1 ORDER BY g.publishTime DESC")
    Page<Goods> findNewArrivals(Pageable pageable);
    
    // 获取同类别商品
    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND g.category = :category AND g.id != :excludeId")
    List<Goods> findRelatedByCategory(@Param("category") String category, @Param("excludeId") Long excludeId);
    
    // 获取同风格商品
    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND g.style = :style AND g.id != :excludeId")
    List<Goods> findRelatedByStyle(@Param("style") String style, @Param("excludeId") Long excludeId);
    
    // 获取同材质商品
    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND g.material = :material AND g.id != :excludeId")
    List<Goods> findRelatedByMaterial(@Param("material") String material, @Param("excludeId") Long excludeId);

}