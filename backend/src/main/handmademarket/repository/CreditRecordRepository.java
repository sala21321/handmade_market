package com.example.handmademarket.repository;

import com.example.handmademarket.entity.CreditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {

}
