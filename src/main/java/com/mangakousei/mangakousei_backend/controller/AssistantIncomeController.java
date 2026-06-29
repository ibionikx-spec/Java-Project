package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.IncomeMonthRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.PaymentService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant/income")
@RequiredArgsConstructor
public class AssistantIncomeController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<IncomeMonthRes>> getMyIncome(
            @RequestParam(required = false) String month) {
        if (!SecurityUtils.isAssistant())
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        Long assistantId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "OK", paymentService.getMyIncome(assistantId, month)));
    }
}