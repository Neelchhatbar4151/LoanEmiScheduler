package com.tss.LoanEmiScheduler.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource){
        super(resource + " Not Found.");
    }
}
