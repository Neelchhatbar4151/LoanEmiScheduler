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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FlatLoanStrategy implements ILoanStrategy {

    private final EmiRepository emiRepo;
    private final PaymentAllocationRepository paymentAllocationRepo;
    private final LoanRepository loanRepo;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    @Override
    public LoanStrategy getType() {
        return LoanStrategy.FLAT;
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

        if(emiRepo.existsByLoanId(loan.getId())){
            throw new ScheduleAlreadyExistsException();
        }

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

    private BigDecimal calculateFlatEmi(BigDecimal principal,
                                        BigDecimal annualRate,
                                        Integer tenureMonths) {

        BigDecimal years = BigDecimal.valueOf(tenureMonths)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal interest = principal
                .multiply(annualRate)
                .multiply(years)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal totalAmount = principal.add(interest);

        return totalAmount.divide(
                BigDecimal.valueOf(tenureMonths),
                2,
                RoundingMode.HALF_UP
        );
    }


    @Override
    public List<Emi> reAmortize(Loan loan, Emi triggerEmi) {
        if (!emiRepo.existsByLoanId(loan.getId())) {
            throw new ResourceNotFoundException("Schedule");
        }

        // 1. Fetch active EMIs (current version)
        List<Emi> emis = new ArrayList<>(emiRepo.findByLoanIdAndIsActive(loan.getId(), true));

        // 2. Sort by installment_no
        emis.sort(Comparator.comparingInt(Emi::getInstallmentNo));

        // 3. Identify future EMIs
        List<Emi> futureEmis = emis.stream()
                .filter(e -> e.getInstallmentNo() > triggerEmi.getInstallmentNo())
                .toList();

        // LAST EMI CASE
        if (futureEmis.isEmpty()) {
            throw new AmortizationNotPossibleException();
        }

        // 5. CAPITALIZATION (OVERDUE / PARTIAL)
        if (triggerEmi.getEmiStatus() == EmiStatus.OVERDUE) {

            BigDecimal remainingInterest =
                    triggerEmi.getInterestComponent().subtract(
                            (paymentAllocationRepo.findByEmiIdAndPaymentAllocationType(triggerEmi.getId(), PaymentAllocationType.INTEREST)
                                    .stream()
                                    .map(PaymentAllocation::getAmountAllocated))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            if (remainingInterest.compareTo(BigDecimal.ZERO) > 0) {

                loan.setOutstandingBalance(
                        loan.getOutstandingBalance().add(remainingInterest)
                );

//                triggerEmi.setCapitalizedInterest(
//                        triggerEmi.getCapitalizedInterest() + remainingInterest
//                );
            }
        }

        // 6. OVERPAYMENT HANDLING
        BigDecimal idealPayment = BigDecimal.ZERO;
        BigDecimal emiPaidAmount = paymentAllocationRepo.findByEmiId(triggerEmi.getId())
                .stream()
                .map(PaymentAllocation::getAmountAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(triggerEmi.getPenalty() != null){
            idealPayment = idealPayment.add(triggerEmi.getPenalty().getPenaltyAmount());
        }

        idealPayment = idealPayment
                .add(triggerEmi.getPenalInterest())
                .add(triggerEmi.getInterestComponent())
                .add(triggerEmi.getPrincipalComponent());

        BigDecimal extraPayment = emiPaidAmount.subtract(idealPayment);

        if (extraPayment.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal newBalance = loan.getOutstandingBalance().subtract(extraPayment);

            loan.setOutstandingBalance(newBalance.max(BigDecimal.ZERO));
        }

        // 7. RE-AMORTIZE FUTURE EMIs
        int remainingTenure = futureEmis.size();

        BigDecimal newEmiAmount = calculateFlatEmi(
                loan.getOutstandingBalance(),
                loan.getInterestRate(),
                remainingTenure
        );

        // deactivate old EMIs
        for (Emi emi : futureEmis) {
            emi.setIsActive(false);
        }
        emiRepo.saveAll(futureEmis);

        // 8. CREATE NEW EMIs (VERSIONING)
        int newVersion = futureEmis.get(0).getVersion() + 1;
        BigDecimal balance = loan.getOutstandingBalance();

        List<Emi> newEmis = new ArrayList<>();

        for (int i = 0; i < remainingTenure; i++) {

            BigDecimal monthlyRate = loan.getInterestRate()
                    .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

            BigDecimal interest = balance.multiply(monthlyRate);
            BigDecimal principal = newEmiAmount.subtract(interest);

            Emi emi = new Emi();
            emi.setLoan(loan);
            emi.setInstallmentNo(triggerEmi.getInstallmentNo() + 1 + i);
            emi.setEmiAmount(newEmiAmount);
            emi.setPrincipalComponent(principal);
            emi.setInterestComponent(interest);
            emi.setVersion(newVersion);
            emi.setIsActive(true);
            if(newEmiAmount.compareTo(BigDecimal.ZERO) == 0)
                emi.setEmiStatus(EmiStatus.CANCELLED);
            else
                emi.setEmiStatus(EmiStatus.PENDING);

            newEmis.add(emi);

            balance = balance.subtract(principal);
        }

        emiRepo.saveAll(newEmis);

        loanRepo.save(loan);

        return newEmis;
    }
}
