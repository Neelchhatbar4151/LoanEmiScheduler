package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.TransactionRequestDto;
import com.tss.LoanEmiScheduler.dto_mapper.TransactionMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepo;
    private final EmiRepository emiRepo;
    private final LoanRepository loanRepo;
    private final PaymentAllocationRepository paymentAllocationRepo;
    private final UserRepository userRepository;

    private final LoanStrategyFactory strategyFactory;

    private final TransactionMapper transactionMapper;

    private final PaymentAllocationService paymentAllocationService;

    @Transactional
    public String pay(TransactionRequestDto txn){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }

        Borrower borrower = ((Borrower) user);
        Loan loan = loanRepo.findByLoanNumberAndBorrowerAccountNumber(
                txn.getLoanNumber(),
                borrower.getAccountNumber()
        ).orElseThrow(()->new ResourceNotFoundException("Loan"));

        if( loan.getLoanStatus() == LoanStatus.CLOSED  ||
            loan.getLoanStatus() == LoanStatus.APPLIED ||
            loan.getLoanStatus() == LoanStatus.REJECTED){
            throw new UnsupportedOperationException("This loan can't accept payments.");
        }

        List<Emi> emis = emiRepo
                .findOverDueEmisByLoan(loan, LocalDate.now());
        List<Emi> latestEmi = emiRepo
                .findCurrentEmiByLoan(loan.getId(), LocalDate.now());

        emis.addAll(latestEmi);

        Emi lastEmi = emis.get(emis.size()-1);

        Transaction transaction = transactionMapper.toEntity(txn, loan);

        transactionRepo.save(transaction);

        BigDecimal remainingAmount = paymentAllocationService.allocate(transaction);

        if(remainingAmount.compareTo(BigDecimal.ZERO) == 0){
            return "Transaction Successful.";
        }

        BigDecimal extraAmount = BigDecimal.ZERO.max(
                remainingAmount
                .subtract(loan.getOutstandingBalance())
        );

        PaymentAllocation pa = new PaymentAllocation();
        pa.setTransaction(transaction);
        pa.setEmi(lastEmi);
        pa.setAmountAllocated(remainingAmount.subtract(extraAmount));
        pa.setPaymentAllocationType(PaymentAllocationType.PRINCIPAL);
        paymentAllocationRepo.save(pa);

        loan.setOutstandingBalance(
                loan.getOutstandingBalance()
                .subtract(remainingAmount.subtract(extraAmount))
        );

        loanRepo.save(loan);
        strategyFactory.getStrategy(loan.getLoanStrategy()).reAmortize(lastEmi);

        //Notification for Extra amount getting credited in borrower account balance;

        return "Transaction Successful, Extra Payment: " + extraAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
