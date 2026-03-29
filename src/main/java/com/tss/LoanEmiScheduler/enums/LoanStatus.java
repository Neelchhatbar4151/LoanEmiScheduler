package com.tss.LoanEmiScheduler.enums;

import com.tss.LoanEmiScheduler.action_service.LoanActionService;
import com.tss.LoanEmiScheduler.entity.Loan;

public enum LoanStatus {

    APPLIED {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleApplied(loan);
        }
    },

    REJECTED {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleRejected(loan);
        }
    },

    ACTIVE {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleActive(loan);
        }
    },

    OVERDUE {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleOverdue(loan);
        }
    },

    DELINQUENT {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleDelinquent(loan);
        }
    },

    NPA {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleNpa(loan);
        }
    },

    CLOSED {
        @Override
        public void handleAndSet(Loan loan, LoanActionService service) {
            service.handleClosed(loan);
        }
    };

    public abstract void handleAndSet(Loan loan, LoanActionService service);
}


//WRITTEN_OFF