package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.entity.Borrower;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StrategySuggestionService {

    private BigDecimal generateBaseEmi(Loan loan){

        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getInterestRate();
        int tenureMonths = loan.getTenure();

        BigDecimal rate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal tenureYears = BigDecimal.valueOf(tenureMonths)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal totalInterest = principal.multiply(rate).multiply(tenureYears);

        BigDecimal totalAmount = principal.add(totalInterest);

        return totalAmount.divide(
                BigDecimal.valueOf(tenureMonths),
                4,
                RoundingMode.HALF_UP
        );
    }

    public LoanStrategy getSuggestedStrategy(Loan loan){
        BigDecimal baseEmi = generateBaseEmi(loan);

        Borrower borrower = loan.getBorrower();

        BigDecimal existingMonthlyDebt = borrower.getDebtAmount().multiply(new BigDecimal("0.25"));

        BigDecimal newMonthlyDebt = existingMonthlyDebt.add(baseEmi);

        BigDecimal dtiRatio = newMonthlyDebt.divide(borrower.getAnnualIncome().divide(new BigDecimal("12"), 15, RoundingMode.HALF_UP), 15, RoundingMode.HALF_UP);

        if(dtiRatio.compareTo(new BigDecimal("0.2")) < 0){
            return LoanStrategy.FLAT;
        }
        else if(dtiRatio.compareTo(new BigDecimal("0.4")) <= 0){
            if(loan.getTenure() < 24){
                return LoanStrategy.REDUCING;
            }

            return LoanStrategy.STEP_UP;
        }

        return LoanStrategy.REJECT;
    }
}
