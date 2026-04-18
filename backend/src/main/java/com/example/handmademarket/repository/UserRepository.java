package com.example.handmademarket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.handmademarket.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserAccount(String useraccount);

    boolean existsByUserAccount(String useraccount);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Optional<User> findByUserAccountOrPhone(String userAccount, String phone);
}
