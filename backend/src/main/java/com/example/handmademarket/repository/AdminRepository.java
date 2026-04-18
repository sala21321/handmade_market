package com.example.handmademarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.handmademarket.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByAdminAccount(String adminAccount);

}