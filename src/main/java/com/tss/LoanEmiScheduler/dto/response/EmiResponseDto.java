package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.EmiStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmiResponseDto {

    private Long loanId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private BigDecimal emiAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal penaltyAmount;
    private BigDecimal penaltyRemaining;
    private EmiStatus emiStatus;
    private Integer version;
    private Boolean isActive=false;
}
