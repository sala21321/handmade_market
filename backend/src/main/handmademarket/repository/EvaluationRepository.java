package com.example.handmademarket.repository;

import com.example.handmademarket.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, String> {

    boolean existsByOrderId(String orderId);

    List<Evaluation> findByOrderId(String orderId);

    List<Evaluation> findByEvaluatedIdOrderByCreateTimeDesc(Integer evaluatedId);

    @Query("SELECT e FROM Evaluation e WHERE e.goodsId = :goodsId AND e.status = 0 ORDER BY e.createTime DESC")
    List<Evaluation> findByGoodsIdAndStatusValid(@Param("goodsId") Long goodsId);

    @Query("SELECT e FROM Evaluation e WHERE e.evaluatedId = :userId AND e.status = 0 ORDER BY e.createTime DESC")
    List<Evaluation> findByUserIdAndStatusValid(@Param("userId") Long userId);

    @Query("SELECT e FROM Evaluation e WHERE e.status = 1 ORDER BY e.createTime DESC")
    List<Evaluation> findViolationReports();

    Optional<Evaluation> findByEvalId(String evalId);
}
