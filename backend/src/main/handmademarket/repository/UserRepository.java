package com.example.handmademarket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.handmademarket.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserAccount(String useraccount);

    boolean existsByUserAccount(String useraccount);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Optional<User> findByUserAccountOrPhone(String userAccount, String phone);

    Optional<User> findByUserId(Integer userId);

     // 根据信用分查找用户
    List<User> findByCreditScoreGreaterThanEqual(Integer minCreditScore);
    
    // 根据邮箱查找
    User findByEmail(String email);
}