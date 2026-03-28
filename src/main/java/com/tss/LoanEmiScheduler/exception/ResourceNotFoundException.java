package com.tss.LoanEmiScheduler.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(){
        super("Schedule Not Found.");
    }
}
