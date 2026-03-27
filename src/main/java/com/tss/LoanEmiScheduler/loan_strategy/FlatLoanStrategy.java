package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class FlatLoanStrategy implements LoanStrategy {

    @Override
    public LoanStrategy getType() {
        return LoanStrategy.FLAT;
    }

    @Override
    public LoanResponseDto getEmiSchedule(Loan loan, LocalDate asOfDate) {
        return null;
    }

    @Override
    public List<Emi> generateSchedule(Loan loan) {
        return List.of();
    }

    @Override
    public List<Emi> reAmortize(Loan loan) {
        return List.of();
    }
}
