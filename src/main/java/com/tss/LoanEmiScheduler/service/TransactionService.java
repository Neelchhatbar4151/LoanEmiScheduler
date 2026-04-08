package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.action_service.LoanActionService;
import com.tss.LoanEmiScheduler.dto.request.TransactionRequestDto;
import com.tss.LoanEmiScheduler.dto_mapper.TransactionMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LogTag;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepo;
    private final EmiRepository emiRepo;
    private final LoanRepository loanRepo;
    private final PaymentAllocationRepository paymentAllocationRepo;
    private final UserRepository userRepository;

    private final LoanActionService loanActionService;

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
        log.info("{} Pay: Emi amount for loan {} by borrower {} of amount {}",
                LogTag.TRANSACTION.getValue(),
                loan.getId(),
                borrower.getId(),
                txn.getTransactionAmount()
        );

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
            log.info("{} Success: On payment of amount {}  for loan {} by borrower {}",
                    LogTag.TRANSACTION.getValue(),
                    txn.getTransactionAmount(),
                    loan.getId(),
                    borrower.getId()
            );
            return "Transaction Successful.";
        }

        BigDecimal extraAmount = BigDecimal.ZERO.max(
                remainingAmount
                .subtract(loan.getOutstandingBalance())
        );

        loan.setOutstandingBalance(
                loan.getOutstandingBalance()
                        .subtract(remainingAmount.subtract(extraAmount))
        );

        borrower.setDebtAmount(
                borrower.getDebtAmount().add(
                        transaction.getTransactionAmount().subtract(extraAmount)
                )
        );

        userRepository.save(borrower);

        List<Emi> remainingOverDueEmis = emiRepo
                .findOverDueEmisByLoan(loan, LocalDate.now());

        if(loan.getLoanStatus() == LoanStatus.NPA ||
                loan.getLoanStatus() == LoanStatus.DELINQUENT ||
                loan.getLoanStatus() == LoanStatus.OVERDUE){
            if(remainingOverDueEmis.isEmpty()){
                loanActionService.handleActive(loan);
            }
            else{
                loanActionService.handleOverdue(loan);
            }
        }

        loanRepo.save(loan);

        if(loan.getLoanStatus() != LoanStatus.CLOSED){
            PaymentAllocation pa = new PaymentAllocation();
            pa.setTransaction(transaction);
            pa.setEmi(lastEmi);
            pa.setAmountAllocated(remainingAmount.subtract(extraAmount));
            pa.setPaymentAllocationType(PaymentAllocationType.PRINCIPAL);
            paymentAllocationRepo.save(pa);

            strategyFactory.getStrategy(loan.getLoanStrategy()).reAmortize(lastEmi);
        }


        //Notification for Extra amount getting credited in borrower account balance;
        String amt = extraAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        log.info("{} Success: On payment of amount {}  for loan {} by borrower {}, extra payment made of amount {}",
                LogTag.TRANSACTION.getValue(),
                txn.getTransactionAmount(),
                loan.getId(),
                borrower.getId(),
                amt
        );
        return "Transaction Successful, Extra Payment: " + extraAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
