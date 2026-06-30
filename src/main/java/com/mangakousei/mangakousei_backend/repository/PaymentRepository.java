package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByAssistantUserIdAndPaymentMonthOrderByCreatedAtDesc(
            Long assistantId, String paymentMonth);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.assistant.userId = :assistantId
          AND p.paymentMonth = :month
        """)
    BigDecimal sumAmountByAssistantAndMonth(
            @Param("assistantId") Long assistantId,
            @Param("month") String month);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.assistant.userId = :assistantId
          AND p.paymentMonth = :month
        """)
    BigDecimal sumPrevMonth(
            @Param("assistantId") Long assistantId,
            @Param("month") String month);

    Optional<Payment> findByTaskTaskId(Long taskId);
}