package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.PaymentAllocation;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.exception.AmortizationNotPossibleException;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.exception.ScheduleAlreadyExistsException;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.PaymentAllocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReducingBalanceLoanStrategy implements ILoanStrategy {

    private final EmiRepository emiRepo;
    private final PaymentAllocationRepository paymentAllocationRepo;
    private final LoanRepository loanRepo;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    @Override
    public LoanStrategy getType() {
        return LoanStrategy.REDUCING;
    }

    @Override
    public LoanResponseDto getEmiSchedule(Loan loan, LocalDate asOfDate) {
        List<Emi> schedule = emiRepo.findEmiScheduleAsOfDate(
                loan.getId(),
                asOfDate.plusDays(1).atStartOfDay()
        );

        LoanResponseDto dto = loanMapper.toDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));
        return dto;
    }

    @Override
    public List<Emi> generateSchedule(Loan loan) {

        if (emiRepo.existsByLoanId(loan.getId())) {
            throw new ScheduleAlreadyExistsException();
        }

        List<Emi> emis = new ArrayList<>();

        BigDecimal principal = loan.getPrincipalAmount();
        int tenure = loan.getTenure();

        BigDecimal emiAmount = calculateEmi(principal, loan.getInterestRate(),tenure);

        BigDecimal balance = principal;

        LocalDate startDate = loan.getApprovedAt().toLocalDate();

        for (int i = 1; i <= tenure; i++) {

            BigDecimal monthlyRate = loan.getInterestRate()
                    .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            BigDecimal interest = balance.multiply(monthlyRate);
            BigDecimal principalComponent = emiAmount.subtract(interest);

            // Last EMI adjustment
            if (i == tenure) {
                principalComponent = balance;
                emiAmount = principalComponent.add(interest);
            }

            Emi emi = new Emi();
            emi.setLoan(loan);
            emi.setInstallmentNo(i);
            emi.setDueDate(startDate.plusMonths(i));

            emi.setEmiAmount(emiAmount);
            emi.setInterestComponent(interest);
            emi.setPrincipalComponent(principalComponent);

            emi.setEmiStatus(EmiStatus.PENDING);
            emi.setVersion(1);
            emi.setIsActive(true);

            emis.add(emi);

            balance = balance.subtract(principalComponent);
        }

        return emis;
    }

    private BigDecimal calculateEmi(BigDecimal principal,
                                    BigDecimal annualRate,
                                    Integer tenureMonths) {

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    BigDecimal.valueOf(tenureMonths),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        MathContext mc = new MathContext(15, RoundingMode.HALF_UP);

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12), mc)
                .divide(BigDecimal.valueOf(100), mc);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, mc);
        BigDecimal power = onePlusR.pow(tenureMonths, mc);

        BigDecimal numerator = principal
                .multiply(monthlyRate, mc)
                .multiply(power, mc);

        BigDecimal denominator = power.subtract(BigDecimal.ONE, mc);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<Emi> reAmortize(Emi triggerEmi) {
        Loan loan = triggerEmi.getLoan();

        if (!emiRepo.existsByLoanId(loan.getId())) {
            throw new ResourceNotFoundException("Schedule");
        }

        List<Emi> emis = new ArrayList<>(emiRepo.findByLoanIdAndIsActive(loan.getId(), true));
        emis.sort(Comparator.comparingInt(Emi::getInstallmentNo));

        List<Emi> futureEmis = emis.stream()
                .filter(e -> e.getInstallmentNo() > triggerEmi.getInstallmentNo())
                .toList();

        if (futureEmis.isEmpty()) {
            throw new AmortizationNotPossibleException();
        }

        // CAPITALIZATION
        if (triggerEmi.getEmiStatus() == EmiStatus.OVERDUE) {

            BigDecimal paidInterest = paymentAllocationRepo
                    .findByEmiIdAndPaymentAllocationType(
                            triggerEmi.getId(),
                            PaymentAllocationType.INTEREST
                    )
                    .stream()
                    .map(PaymentAllocation::getAmountAllocated)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remainingInterest = triggerEmi.getInterestComponent()
                    .subtract(paidInterest);

            if (remainingInterest.compareTo(BigDecimal.ZERO) > 0) {
                loan.setOutstandingBalance(
                        loan.getOutstandingBalance().add(remainingInterest)
                );
            }
        }

        // OVERPAYMENT
        BigDecimal emiPaid = paymentAllocationRepo.findByEmiId(triggerEmi.getId())
                .stream()
                .map(PaymentAllocation::getAmountAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ideal = triggerEmi.getInterestComponent()
                .add(triggerEmi.getPrincipalComponent())
                .add(triggerEmi.getPenalInterest() != null
                        ? triggerEmi.getPenalInterest()
                        : BigDecimal.ZERO);

        BigDecimal extra = emiPaid.subtract(ideal);

        if (extra.compareTo(BigDecimal.ZERO) > 0) {
            loan.setOutstandingBalance(
                    loan.getOutstandingBalance()
                            .subtract(extra)
                            .max(BigDecimal.ZERO)
            );
        }

        int remainingTenure = futureEmis.size();

        BigDecimal newEmi = calculateEmi(
                loan.getOutstandingBalance(),
                loan.getInterestRate(),
                remainingTenure
        );

        // deactivate old EMIs
        futureEmis.forEach(e -> e.setIsActive(false));
        emiRepo.saveAll(futureEmis);

        int newVersion = futureEmis.get(0).getVersion() + 1;
        BigDecimal balance = loan.getOutstandingBalance();

        List<Emi> newEmis = new ArrayList<>();

        for (int i = 0; i < remainingTenure; i++) {

            BigDecimal monthlyRate = loan.getInterestRate()
                    .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            BigDecimal interest = balance.multiply(monthlyRate);
            BigDecimal principal = newEmi.subtract(interest);

            // last EMI adjustment
            if (i == remainingTenure - 1) {
                principal = balance;
                newEmi = principal.add(interest);
            }

            Emi emi = new Emi();
            emi.setLoan(loan);
            emi.setInstallmentNo(triggerEmi.getInstallmentNo() + 1 + i);

            emi.setEmiAmount(newEmi);
            emi.setInterestComponent(interest);
            emi.setPrincipalComponent(principal);

            if(newEmi.compareTo(BigDecimal.ZERO) == 0)
                emi.setEmiStatus(EmiStatus.CANCELLED);
            else
                emi.setEmiStatus(EmiStatus.PENDING);
            emi.setVersion(newVersion);
            emi.setIsActive(true);

            newEmis.add(emi);

            balance = balance.subtract(principal);
        }

        emiRepo.saveAll(newEmis);
        loanRepo.save(loan);

        return newEmis;
    }
}