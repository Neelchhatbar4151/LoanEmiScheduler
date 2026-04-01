package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.enums.NotificationType;
import jakarta.mail.MessagingException;

import java.util.Map;

public interface NotificationService {
    void sendNotification(String to, NotificationType type, Map<String, Object> variables) throws MessagingException;
}
