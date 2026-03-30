package com.tss.LoanEmiScheduler.service;

public interface NotificationService {
    void sendNotification(String to, String subject, String htmlContent) throws Exception;
}
