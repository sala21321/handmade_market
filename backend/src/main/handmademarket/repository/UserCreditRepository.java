package com.example.handmademarket.repository;

import com.example.handmademarket.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {
    // 自带全部基础增删改查、分页能力，开箱即用
}
