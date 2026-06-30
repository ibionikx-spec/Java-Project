package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.status.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {
    Optional<PaymentStatus> findByPaymentStatusName(String name);
}