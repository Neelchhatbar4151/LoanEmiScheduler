package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.*;
import com.tss.LoanEmiScheduler.exception.*;
import com.tss.LoanEmiScheduler.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class StepUpLoanStrategy implements ILoanStrategy {

    private final EmiRepository emiRepo;
    private final PaymentAllocationRepository paymentAllocationRepo;
    private final LoanRepository loanRepo;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    private static final BigDecimal YEARLY_GROWTH = new BigDecimal("1.05");
    private static final MathContext mc = new MathContext(15, RoundingMode.HALF_UP);

    @Override
    public LoanStrategy getType() {
        return LoanStrategy.STEP_UP;
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
        BigDecimal monthlyRate = getMonthlyRate(loan);

        BigDecimal baseEmi = findBaseEmiForStepUp(principal, monthlyRate, tenure);

        BigDecimal balance = principal;
        LocalDate startDate = loan.getApprovedAt().toLocalDate();

        for (int i = 0; i < tenure; i++) {

            int installmentNo = i + 1;
            int yearIndex = (installmentNo - 1) / 12;

            BigDecimal emiAmount = baseEmi.multiply(
                    YEARLY_GROWTH.pow(yearIndex, mc), mc
            );

            BigDecimal interest = balance.multiply(monthlyRate, mc);
            BigDecimal principalComponent = emiAmount.subtract(interest);

            if (i == tenure - 1) {
                principalComponent = balance;
                emiAmount = principalComponent.add(interest);
            }

            Emi emi = new Emi();
            emi.setLoan(loan);
            emi.setInstallmentNo(installmentNo);
            emi.setDueDate(startDate.plusMonths(installmentNo));

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

    // RE-AMORTIZE (STEP ALIGNMENT PRESERVED)
    @Override
    public List<Emi> reAmortize(Loan loan, Emi triggerEmi) {

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
                    loan.getOutstandingBalance().subtract(extra).max(BigDecimal.ZERO)
            );
        }

        int remainingTenure = futureEmis.size();
        BigDecimal newPrincipal = loan.getOutstandingBalance();
        BigDecimal monthlyRate = getMonthlyRate(loan);

        // deactivate old EMIs
        futureEmis.forEach(e -> e.setIsActive(false));
        emiRepo.saveAll(futureEmis);

        int newVersion = futureEmis.get(0).getVersion() + 1;
        LocalDate startDate = loan.getApprovedAt().toLocalDate();

        BigDecimal baseEmi = findBaseEmiForStepUp(
                newPrincipal,
                monthlyRate,
                remainingTenure,
                triggerEmi.getInstallmentNo()
        );

        BigDecimal balance = newPrincipal;
        List<Emi> newEmis = new ArrayList<>();

        for (int i = 0; i < remainingTenure; i++) {

            int installmentNo = triggerEmi.getInstallmentNo() + 1 + i;

            int yearIndex = (installmentNo - 1) / 12;

            BigDecimal emiAmount = baseEmi.multiply(
                    YEARLY_GROWTH.pow(yearIndex, mc), mc
            );

            BigDecimal interest = balance.multiply(monthlyRate, mc);
            BigDecimal principal = emiAmount.subtract(interest);

            if (i == remainingTenure - 1) {
                principal = balance;
                emiAmount = principal.add(interest);
            }

            Emi emi = new Emi();
            emi.setLoan(loan);
            emi.setInstallmentNo(installmentNo);
            emi.setDueDate(startDate.plusMonths(i + 1));

            emi.setEmiAmount(emiAmount);
            emi.setInterestComponent(interest);
            emi.setPrincipalComponent(principal);
            if(emiAmount.compareTo(BigDecimal.ZERO) == 0)
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

    // HELPERS
    private BigDecimal getMonthlyRate(Loan loan) {
        return loan.getInterestRate()
                .divide(BigDecimal.valueOf(12), mc)
                .divide(BigDecimal.valueOf(100), mc);
    }

    private BigDecimal findBaseEmiForStepUp(BigDecimal principal,
                                            BigDecimal monthlyRate,
                                            int tenure) {
        return findBaseEmiForStepUp(principal, monthlyRate, tenure, 0);
    }

    private BigDecimal findBaseEmiForStepUp(BigDecimal principal,
                                            BigDecimal monthlyRate,
                                            int tenure,
                                            int startInstallmentNo) {

        BigDecimal low = BigDecimal.ZERO;
        BigDecimal high = principal;
        BigDecimal result = high;

        for (int i = 0; i < 100; i++) {

            BigDecimal mid = low.add(high).divide(BigDecimal.valueOf(2), mc);

            BigDecimal remaining = simulateStepUp(
                    principal,
                    monthlyRate,
                    tenure,
                    mid,
                    startInstallmentNo
            );

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                low = mid;
            } else {
                result = mid;
                high = mid;
            }
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal simulateStepUp(BigDecimal principal,
                                      BigDecimal monthlyRate,
                                      int tenure,
                                      BigDecimal baseEmi,
                                      int startInstallmentNo) {

        BigDecimal balance = principal;

        for (int i = 0; i < tenure; i++) {

            int installmentNo = startInstallmentNo + 1 + i;
            int yearIndex = (installmentNo - 1) / 12;

            BigDecimal emi = baseEmi.multiply(
                    YEARLY_GROWTH.pow(yearIndex, mc), mc
            );

            BigDecimal interest = balance.multiply(monthlyRate, mc);
            BigDecimal principalComp = emi.subtract(interest);

            balance = balance.subtract(principalComp);
        }

        return balance;
    }
}