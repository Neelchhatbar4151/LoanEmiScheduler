package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentAllocationResponseDto {
    private PaymentAllocationType paymentAllocationType;
    private EmiResponseDto emi;
    private BigDecimal amountAllocated;
}
