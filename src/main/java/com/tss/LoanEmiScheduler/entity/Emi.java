package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.EmiStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "emis",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"loan_id", "installment_no", "version"})
        })
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints =
        "installment_no > 0 " +
        "AND emi_amount>=0 " +
        "AND principal_component >= 0 " +
        "AND interest_component >= 0 " +
        "AND version >= 0 " +
        "AND penal_interest >= 0 " +
        "AND remaining_penal_interest >= 0 " +
        "AND remaining_interest_component >= 0 " +
        "AND remaining_principal_component >= 0"
)
public class Emi extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false, updatable = false)
    private Loan loan;

    @Column(nullable = false, name = "installment_no", updatable = false)
    private Integer installmentNo;

    @FutureOrPresent
    @Column(nullable = false, updatable = false)
    private LocalDate dueDate;

    @Column(name = "emi_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false, name = "principal_component", precision = 19, scale = 2)
    private BigDecimal principalComponent;

    @Column(nullable = false, name = "interest_component", precision = 19, scale = 2)
    private BigDecimal interestComponent;

    @OneToOne
    @JoinColumn(name = "penalty_id")
    private Penalty penalty; //one to many?

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmiStatus emiStatus;

    @Column(nullable = false, name = "version")
    private Integer version = 0;

    @Column(nullable = false)
    private Boolean isActive=false;

    @Column(nullable = false, name = "penal_interest", precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal penalInterest = BigDecimal.ZERO;

    @Column(nullable = false, name = "remaining_principal_component", precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal remainingPrincipalComponent = BigDecimal.ZERO;

    @Column(nullable = false, name = "remaining_interest_component", precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal remainingInterestComponent = BigDecimal.ZERO;

    @Column(nullable = false, name = "remaining_penal_interest", precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal remainingPenalInterest = BigDecimal.ZERO;

//    @Column(nullable = false)
//    private LocalDate lastCalculatedDate;
}
