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
    @Column(nullable = false)
    private NotificationType notificationType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "emi_id")
    private Emi emi;
}
