package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.entity.Borrower;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.LogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("{} Schedule: Generate base LogTag.EMI.getValue() for loan {} on rate {} for years {} on total interest {} for total amount {}",
                LogTag.LOAN.getValue(),
                loan.getId(),
                rate,
                tenureYears,
                totalInterest,
                totalAmount);
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

        log.info("{} Strategy: Suggestion for loan {} dti ration {}", LogTag.LOAN.getValue(), loan.getId(), dtiRatio);
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
