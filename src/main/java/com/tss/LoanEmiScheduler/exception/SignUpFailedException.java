package com.tss.LoanEmiScheduler.exception;

public class SignUpFailedException extends RuntimeException {
    public SignUpFailedException(String message) {
        super("Sign up failed for " + message);
    }
}
