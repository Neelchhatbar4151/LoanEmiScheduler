package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.TransactionMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionRequestDto {
    @NotBlank
    private String loanNumber;
    @NotNull
    @Positive
    private BigDecimal transactionAmount;
    @NotNull
    private TransactionMode transactionMode;
    private String transactionReference;
}
