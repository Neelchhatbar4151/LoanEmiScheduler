package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.LoanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowerLoanResponseDto {
    private String loanNumber;
    private String officerUsername;
    private LocalDateTime disbursementDate;
    private LocalDate closedAt;
    private LocalDate approvedAt;
    private Integer tenure;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private LoanStatus loanStatus;
    private LoanType loanType;
    private LoanStrategy loanStrategy;
    private BigDecimal outstandingBalance;
    private BigDecimal penaltyAmount;
    private BigDecimal penaltyRemaining;
}
