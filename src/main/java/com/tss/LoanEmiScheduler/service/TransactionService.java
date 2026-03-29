package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.TransactionRequestDto;
import com.tss.LoanEmiScheduler.dto_mapper.TransactionMapper;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.PaymentAllocation;
import com.tss.LoanEmiScheduler.entity.Transaction;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.PaymentAllocationRepository;
import com.tss.LoanEmiScheduler.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
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

    private final LoanStrategyFactory strategyFactory;

    private final TransactionMapper transactionMapper;

    private final PaymentAllocationService paymentAllocationService;

    public String pay(TransactionRequestDto txn){

        Loan loan = loanRepo.findById(txn.getLoanId()).orElseThrow(()->new ResourceNotFoundException("Loan"));

        if( loan.getLoanStatus() == LoanStatus.CLOSED  ||
            loan.getLoanStatus() == LoanStatus.APPLIED ||
            loan.getLoanStatus() == LoanStatus.REJECTED){
            throw new UnsupportedOperationException("This loan can't accept payments.");
        }

        List<Emi> emis = emiRepo
                .findEligibleEmisForPayment(loan, LocalDate.now());

        Emi lastEmi = emis.get(emis.size()-1);

        Transaction transaction = transactionMapper.toEntity(txn, loan);

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

        strategyFactory.getStrategy(loan.getLoanStrategy()).reAmortize(loan, lastEmi);

        //Notification for Extra amount getting credited in borrower account balance;

        return "Transaction Successful, Extra Payment: " + extraAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
