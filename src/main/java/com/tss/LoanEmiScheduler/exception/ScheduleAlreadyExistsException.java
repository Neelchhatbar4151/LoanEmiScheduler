package com.tss.LoanEmiScheduler.exception;

public class ScheduleAlreadyExistsException extends RuntimeException {
    public ScheduleAlreadyExistsException(){
        super("Schedule for this loan already exists.");
    }
}
