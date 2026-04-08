package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.TransactionMode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BorrowerTransactionResponseDto {
    private String loanNumber;
    private BigDecimal paidAmount;
    private BigDecimal extraAmount;
    private TransactionMode transactionMode;
    private String transactionReference;
    private LocalDateTime paidAt;
}
