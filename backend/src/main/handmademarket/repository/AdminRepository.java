package com.example.handmademarket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.handmademarket.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByAdminAccount(String adminAccount);

    long countByPermissionLevelAndStatus(Integer level, Integer status);
}