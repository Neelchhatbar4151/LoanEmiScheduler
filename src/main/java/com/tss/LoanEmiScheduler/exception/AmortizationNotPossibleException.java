package com.tss.LoanEmiScheduler.exception;

public class AmortizationNotPossibleException extends RuntimeException {
    public AmortizationNotPossibleException() {
        super("Amortization Not Possible");
    }
}
