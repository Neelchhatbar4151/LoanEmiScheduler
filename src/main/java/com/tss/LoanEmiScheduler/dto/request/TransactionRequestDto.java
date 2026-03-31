package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.TransactionMode;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDto {
    private Long loanId;
    private BigDecimal transactionAmount;
    private TransactionMode transactionMode;
    private String transactionReference;
}
