package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.LoanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "loans")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Loan extends BaseEntity{
    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @ManyToOne
    @JoinColumn(name = "officer_id", nullable = false)
    private Officer officer; //officer that manages this loan

    @PastOrPresent
    private LocalDateTime approvedAt;

    @FutureOrPresent
    private LocalDate closedAt;

    @Column(nullable = false)
    private Integer tenure; //num of months

    @Column(nullable = false)
    private Double principleAmount;

    @Column(nullable = false)
    private Double interestRate;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus loanStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStrategy loanStrategy;

    @Column(nullable = false)
    private Double outstandingBalance;

    @OneToOne
    @JoinColumn(name = "penalty_id")
    private Penalty penalty;
}
