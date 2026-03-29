package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.TransactionMode;

import java.math.BigDecimal;

public class TransactionRequestDto {
    private Long loanId;
    private BigDecimal transactionAmount;
    private TransactionMode transactionMode;
    private String transactionReference;
}
