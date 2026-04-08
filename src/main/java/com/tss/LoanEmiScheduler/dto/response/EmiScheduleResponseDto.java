package com.tss.LoanEmiScheduler.dto.response;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.LoanType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EmiScheduleResponseDto {
    private String loanNumber;
    private Integer tenure;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private LoanType loanType;
    private LoanStrategy simulationStrategy;
    private List<EmiResponseDto> emis;
}
