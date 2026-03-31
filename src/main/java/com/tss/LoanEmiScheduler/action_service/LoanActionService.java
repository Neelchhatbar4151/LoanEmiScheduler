package com.tss.LoanEmiScheduler.action_service;

import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanActionService {
    private final LoanRepository loanRepo;

    private void checkIfApplied(Loan loan){
        if(loan.getLoanStatus() == LoanStatus.APPLIED){
            throw new UnsupportedOperationException("Loan has not been approved yet.");
        }
    }

    private void checkIfClosed(Loan loan){
        if(loan.getLoanStatus() == LoanStatus.CLOSED){
            throw new UnsupportedOperationException("Loan has been closed.");
        }
    }

    public void handleApplied(Loan loan) {
        checkIfClosed(loan);

        loan.setLoanStatus(LoanStatus.APPLIED);
        loanRepo.save(loan);
    }

    public void handleRejected(Loan loan) {
        if(loan.getLoanStatus() == LoanStatus.APPLIED){
            loan.setLoanStatus(LoanStatus.REJECTED);
        }
        else{
            throw new UnsupportedOperationException("Can't set a Running loan Rejected.");
        }

        loanRepo.save(loan);
    }

    public void handleOverdue(Loan loan) {
        checkIfClosed(loan);
        checkIfApplied(loan);

        loan.setLoanStatus(LoanStatus.OVERDUE);
        loanRepo.save(loan);
    }

    public void handleActive(Loan loan) {
        checkIfClosed(loan);

        loan.setLoanStatus(LoanStatus.ACTIVE);
        loanRepo.save(loan);
    }

    public void handleClosed(Loan loan) {
        checkIfApplied(loan);

        if(loan.getLoanStatus() != LoanStatus.ACTIVE){
            throw new UnsupportedOperationException("You can only close an Active loan.");
        }

        loan.setLoanStatus(LoanStatus.CLOSED);
        loanRepo.save(loan);
    }
    
    public void handleNpa(Loan loan){
        checkIfApplied(loan);
        checkIfClosed(loan);

        loan.setLoanStatus(LoanStatus.NPA);
        loanRepo.save(loan);
    }
    
    public void handleDelinquent(Loan loan){
        checkIfApplied(loan);
        checkIfClosed(loan);

        loan.setLoanStatus(LoanStatus.DELINQUENT);
        loanRepo.save(loan);
    }
}
