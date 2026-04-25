package com.example.handmademarket.repository;

import com.example.handmademarket.entity.Custom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomRepository extends JpaRepository<Custom, Integer> {

    List<Custom> findByConsumerIdOrderBySubmitTimeDesc(Integer consumerId);

    List<Custom> findByCreatorIdOrderBySubmitTimeDesc(Integer creatorId);

    List<Custom> findByStatusOrderBySubmitTimeDesc(Integer status);

    List<Custom> findByStatusInOrderBySubmitTimeDesc(List<Integer> statuses);

    List<Custom> findAllByOrderBySubmitTimeDesc();

    List<Custom> findByCategoryOrderBySubmitTimeDesc(String category);
}
