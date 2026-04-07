package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.enums.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.EMAIL;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService{

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

//    @Override
//    public void sendNotification(String to, String subject, String htmlContent) throws Exception {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(htmlContent, true);
//
//        mailSender.send(message);
//    }


    @Async
    @Override
    public void sendNotification(String to, NotificationType type, Map<String, Object> variables)
            throws MessagingException {
        log.info("{} Create: Notification to {} for {}", EMAIL, to, type);
        Context context = new Context();
        context.setVariables(variables);

        // Map template + subject
        String template = "";
        String subject = "";
        String contentTemplate = "";

        switch (type) {
            case REMINDER:
                subject = "EMI Reminder";
                contentTemplate = "email/emiReminder";
                break;

            case OVERDUE:
                subject = "EMI Overdue Alert";
                contentTemplate = "email/emiOverdue";
                break;

            case APPLICATION:
                subject = "Loan Application Submitted";
                contentTemplate = "email/loanSubmitted";
                break;

            case APPROVAL:
                subject = "Loan Approved 🎉";
                contentTemplate = "email/loanApproved";
                break;

            case REJECTION:
                subject = "Loan Rejected";
                contentTemplate = "email/loanRejected";
                break;

            case INFO:
                subject = (String)context.getVariable("subject");
                contentTemplate = "email/infoLayout";
                break;
        }
        context.setVariable("contentTemplate", contentTemplate);
        context.setVariable("title", subject);
        log.info("{} Metadata: Notification subject: {} contentTemplate: {}", EMAIL, context.getVariable("title"), context.getVariable("contentTemplate"));
        String html = templateEngine.process("email/layout", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        log.info("{} Created: MimeMessageHelper object created with receiver: {}, subject: {}, text: {}", EMAIL, to, subject, html);
        mailSender.send(message);
    }
}
