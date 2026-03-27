package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "notifications")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseEntity{
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false, updatable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_id", updatable = false)
    private Emi emi;
}
