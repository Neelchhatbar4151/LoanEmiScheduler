package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoanApplyResponseDto {
    private String loanNumber;
    private LoanStatus status;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private Integer tenure;
}
