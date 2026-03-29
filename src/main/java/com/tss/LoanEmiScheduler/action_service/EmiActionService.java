package com.tss.LoanEmiScheduler.action_service;

import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmiActionService {
    private final EmiRepository emiRepo;
    private final LoanActionService loanActionService;

    private void checkIfCancelled(Emi emi){
        if(emi.getEmiStatus() == EmiStatus.CANCELLED){
            throw new UnsupportedOperationException("Emi is Cancelled.");
        }
    }

    private void checkIfPaid(Emi emi){
        if(emi.getEmiStatus() == EmiStatus.PAID){
            throw new UnsupportedOperationException("Emi is Fully Paid, can't modify anything.");
        }
    }

    public void handlePending(Emi emi) {
        checkIfCancelled(emi);
        checkIfPaid(emi);

        emi.setEmiStatus(EmiStatus.PENDING);
        emiRepo.save(emi);
    }

    public void handlePartial(Emi emi) {
        checkIfCancelled(emi);
        checkIfPaid(emi);

        if( emi.getEmiStatus() != EmiStatus.OVERDUE){
            emi.setEmiStatus(EmiStatus.PARTIALLY_PAID);
        }

        emiRepo.save(emi);
    }

    public void handlePaid(Emi emi) {
        checkIfCancelled(emi);

        emi.setEmiStatus(EmiStatus.PAID);
        if(emi.getLoan().getTenure().equals(emi.getInstallmentNo())){
            LoanStatus.CLOSED.handleAndSet(emi.getLoan(), loanActionService);
        }

        emiRepo.save(emi);
    }

    public void handleOverdue(Emi emi) {
        checkIfCancelled(emi);
        checkIfPaid(emi);

        emi.setEmiStatus(EmiStatus.OVERDUE);

        if(emi.getLoan().getLoanStatus() == LoanStatus.ACTIVE){
            LoanStatus.OVERDUE.handleAndSet(emi.getLoan(), loanActionService);
        }

        emiRepo.save(emi);
    }

    public void handleCancelled(Emi emi) {
        checkIfPaid(emi);

        emi.setEmiStatus(EmiStatus.CANCELLED);
        if(emi.getInstallmentNo().equals(emi.getLoan().getTenure())){
            LoanStatus.CLOSED.handleAndSet(emi.getLoan(), loanActionService);
        }

        emiRepo.save(emi);
    }
}

