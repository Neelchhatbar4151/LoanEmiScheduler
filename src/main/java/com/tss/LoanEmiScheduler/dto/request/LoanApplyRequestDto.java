package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoanApplyRequestDto {
    @NotNull
    @Positive
    private Integer tenure; //num of months

    @NotNull
    @Positive
    private BigDecimal principalAmount;

    @NotNull
    private LoanType loanType;
}
