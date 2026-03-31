package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;

import java.time.LocalDate;
import java.util.List;

public interface ILoanStrategy {
    LoanStrategy getType();

    LoanResponseDto getEmiSchedule(Loan loan, LocalDate asOfDate);

    List<Emi> generateSchedule(Loan loan);

    List<Emi> reAmortize(Emi triggerEmi);
}
