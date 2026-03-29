package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.PaymentAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentAllocationService {

    private final EmiRepository emiRepository;
    private final LoanRepository loanRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    @Transactional
    public BigDecimal allocate(Transaction txn) {

        BigDecimal remaining = txn.getTransactionAmount();
        Loan loan = txn.getLoan();

        List<Emi> emis = emiRepository
                .findEligibleEmisForPayment(loan, LocalDate.now());

        for (Emi emi : emis) {

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            remaining = allocatePenalInterest(txn, emi, remaining);
            remaining = allocateEmiPenalty(txn, emi, remaining);
            remaining = allocateInterest(txn, emi, remaining);
            remaining = allocatePrincipal(txn, loan, emi, remaining);

            updateEmiStatus(emi);
        }

        // Loan-level penalty
        if (remaining.compareTo(BigDecimal.ZERO) > 0 && loan.getPenalty() != null) {
            remaining = allocateLoanPenalty(txn, loan, remaining);
        }

        loanRepository.save(loan);
        return remaining;
    }

    //Specific Component Allocations
    private BigDecimal allocatePenalInterest(Transaction txn, Emi emi, BigDecimal remaining) {

        BigDecimal due = emi.getRemainingPenalInterest();
        if (due.compareTo(BigDecimal.ZERO) <= 0) return remaining;

        BigDecimal pay = remaining.min(due);

        saveAllocation(txn, emi, PaymentAllocationType.PENAL_INTEREST, pay);
        emi.setRemainingPenalInterest(due.subtract(pay));

        return remaining.subtract(pay);
    }

    private BigDecimal allocateEmiPenalty(Transaction txn, Emi emi, BigDecimal remaining) {

        if (emi.getPenalty() == null) return remaining;

        BigDecimal due = emi.getPenalty().getRemainingAmount();
        if (due.compareTo(BigDecimal.ZERO) <= 0) return remaining;

        BigDecimal pay = remaining.min(due);

        saveAllocation(txn, emi, PaymentAllocationType.PENALTY, pay);
        emi.getPenalty().setRemainingAmount(due.subtract(pay));

        return remaining.subtract(pay);
    }

    private BigDecimal allocateInterest(Transaction txn, Emi emi, BigDecimal remaining) {

        BigDecimal due = emi.getRemainingInterestComponent();
        if (due.compareTo(BigDecimal.ZERO) <= 0) return remaining;

        BigDecimal pay = remaining.min(due);

        saveAllocation(txn, emi, PaymentAllocationType.INTEREST, pay);
        emi.setRemainingInterestComponent(due.subtract(pay));

        return remaining.subtract(pay);
    }

    private BigDecimal allocatePrincipal(Transaction txn, Loan loan, Emi emi, BigDecimal remaining) {

        BigDecimal due = emi.getRemainingPrincipalComponent();
        if (due.compareTo(BigDecimal.ZERO) <= 0) return remaining;

        BigDecimal pay = remaining.min(due);

        saveAllocation(txn, emi, PaymentAllocationType.PRINCIPAL, pay);

        emi.setRemainingPrincipalComponent(due.subtract(pay));

        loan.setOutstandingBalance(
                loan.getOutstandingBalance().subtract(pay)
        );

        return remaining.subtract(pay);
    }

    private BigDecimal allocateLoanPenalty(Transaction txn, Loan loan, BigDecimal remaining) {

        BigDecimal due = loan.getPenalty().getRemainingAmount();
        if (due.compareTo(BigDecimal.ZERO) <= 0) return remaining;

        BigDecimal pay = remaining.min(due);

        saveAllocation(txn, null, PaymentAllocationType.LOAN_PENALTY, pay);
        loan.getPenalty().setRemainingAmount(due.subtract(pay));

        return remaining.subtract(pay);
    }

    // Helpers
    private void saveAllocation(Transaction txn,
                                Emi emi,
                                PaymentAllocationType type,
                                BigDecimal amount) {

        PaymentAllocation pa = new PaymentAllocation();
        pa.setTransaction(txn);
        pa.setEmi(emi);
        pa.setPaymentAllocationType(type);
        pa.setAmountAllocated(amount);

        paymentAllocationRepository.save(pa);
    }

    private void updateEmiStatus(Emi emi) {

        boolean principalDone = emi.getRemainingPrincipalComponent().compareTo(BigDecimal.ZERO) == 0;
        boolean interestDone = emi.getRemainingInterestComponent().compareTo(BigDecimal.ZERO) == 0;
        boolean penalDone = emi.getRemainingPenalInterest().compareTo(BigDecimal.ZERO) == 0;

        boolean penaltyDone = (emi.getPenalty() == null) ||
                emi.getPenalty().getRemainingAmount().compareTo(BigDecimal.ZERO) == 0;

        if (principalDone && interestDone && penalDone && penaltyDone) {
            emi.setEmiStatus(EmiStatus.PAID);
        } else {
            emi.setEmiStatus(EmiStatus.PARTIALLY_PAID);
        }

        emiRepository.save(emi);
    }
}
