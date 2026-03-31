package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.LoanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "loans")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "tenure > 0 AND " +
        "principal_amount > 0 AND " +
        "interest_rate >= 0 AND " +
        "outstanding_balance >= 0 AND " +
        "outstanding_balance <= principal_amount")
@Audited
@AuditOverride(forClass = BaseEntity.class, isAudited = true)
public class Loan extends BaseEntity{
    @Column(unique = true, nullable = false)
    private String loanNumber;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false, updatable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Borrower borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id") //null when in applied state
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Officer officer; //officer who manages this loan

    @PastOrPresent
    private LocalDateTime approvedAt;

    @FutureOrPresent
    private LocalDate closedAt;

    @Column(nullable = false, updatable = false)
    @Positive
    private Integer tenure; //num of months

    @Column(nullable = false, updatable = false)
    @Positive
    private BigDecimal principalAmount;

    @Column(nullable = false)
    @PositiveOrZero
    private BigDecimal interestRate; //annual

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus loanStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column
    private LoanStrategy loanStrategy;

    @Column(nullable = false)
    @PositiveOrZero
    private BigDecimal outstandingBalance;

    @OneToOne //One to many?
    @JoinColumn(name = "penalty_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Penalty penalty;
}
