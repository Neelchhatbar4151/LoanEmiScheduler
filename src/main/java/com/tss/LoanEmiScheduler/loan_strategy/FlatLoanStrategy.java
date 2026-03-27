package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlatLoanStrategy implements ILoanStrategy {

    private final EmiRepository emiRepo;
    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    @Override
    public LoanStrategy getType() {
        return LoanStrategy.FLAT;
    }

    @Override
    public LoanResponseDto getEmiSchedule(Loan loan, LocalDate asOfDate) {
        List<Emi> schedule = emiRepo.findEmiScheduleAsOfDate(loan.getId(), asOfDate.plusDays(1).atStartOfDay());
        LoanResponseDto dto = loanMapper.toDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));
        return dto;
    }

    @Override
    public List<Emi> generateSchedule(Loan loan) {

        List<Emi> emis = new ArrayList<>();

        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getInterestRate();
        int tenureMonths = loan.getTenure();

        BigDecimal rate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal tenureYears = BigDecimal.valueOf(tenureMonths)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal totalInterest = principal.multiply(rate).multiply(tenureYears);

        // Total payable
        BigDecimal totalAmount = principal.add(totalInterest);

        // EMI = total / months
        BigDecimal emiAmount = totalAmount.divide(
                BigDecimal.valueOf(tenureMonths),
                4,
                RoundingMode.HALF_UP
        );

        // Monthly components (fixed in flat loan)
        BigDecimal monthlyPrincipal = principal.divide(
                BigDecimal.valueOf(tenureMonths),
                4,
                RoundingMode.HALF_UP
        );

        BigDecimal monthlyInterest = totalInterest.divide(
                BigDecimal.valueOf(tenureMonths),
                4,
                RoundingMode.HALF_UP
        );

        // Start Date (can have a separate disbursement date too)
        LocalDate startDate = loan.getApprovedAt().toLocalDate();

        for (int i = 1; i <= tenureMonths; i++) {

            Emi emi = new Emi();
            emi.setLoan(loan);
            emi.setInstallmentNo(i);

            emi.setDueDate(startDate.plusMonths(i));

            emi.setEmiAmount(emiAmount);
            emi.setPrincipalComponent(monthlyPrincipal);
            emi.setInterestComponent(monthlyInterest);

            emi.setEmiStatus(EmiStatus.PENDING);
            emi.setVersion(1);
            emi.setIsActive(true);

            emis.add(emi);
        }

        return emis;
    }

    @Override
    public List<Emi> reAmortize(Loan loan) {
        return List.of();
    }
}
