package com.tss.LoanEmiScheduler.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmailNotificationServiceTest {
    @Autowired
    private EmailNotificationService emailService;
    @Test
    void sendNotification() throws Exception {

//        emailService.sendNotification(
//                "neel.chhantbar@tssconsultancy.com",
//                "Test Email",
//                "Hello from Spring Boot!"
//        );
    }
}