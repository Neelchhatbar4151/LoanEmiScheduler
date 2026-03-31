package com.tss.LoanEmiScheduler.enums;

import com.tss.LoanEmiScheduler.action_service.EmiActionService;
import com.tss.LoanEmiScheduler.entity.Emi;

public enum EmiStatus {

    PENDING {
        @Override
        public void handleAndSet(Emi emi, EmiActionService service) {
            service.handlePending(emi);
        }
    },

    PARTIALLY_PAID {
        @Override
        public void handleAndSet(Emi emi, EmiActionService service) {
            service.handlePartial(emi);
        }
    },

    PAID {
        @Override
        public void handleAndSet(Emi emi, EmiActionService service) {
            service.handlePaid(emi);
        }
    },

    OVERDUE {
        @Override
        public void handleAndSet(Emi emi, EmiActionService service) {
            service.handleOverdue(emi);
        }
    },

    CANCELLED {
        @Override
        public void handleAndSet(Emi emi, EmiActionService service) {
            service.handleCancelled(emi);
        }
    };

    public abstract void handleAndSet(Emi emi, EmiActionService service);

}