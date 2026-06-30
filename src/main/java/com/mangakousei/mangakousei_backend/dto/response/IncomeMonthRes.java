package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class IncomeMonthRes {
    private String     month;
    private String     monthLabel;
    private BigDecimal totalAmount;
    private BigDecimal prevMonthAmount;
    private int        taskCount;
    private List<PaymentRes> payments;
}