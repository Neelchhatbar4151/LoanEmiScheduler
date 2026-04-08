package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.LoanType;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
public class OfficerAppliedLoanResponseDto {
    private String loanNumber;
    private String borrowerAccountNumber;
    private LocalDate createdAt;
    private Integer tenure;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private String branchCode;
    private LoanStatus loanStatus;
    private LoanType loanType;
    private List<EmiResponseDto> emiResponseDtoList;
}

