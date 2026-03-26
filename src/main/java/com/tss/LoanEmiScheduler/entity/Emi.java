package com.tss.LoanEmiScheduler.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalDate;

@Table(name = "emis")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints =
        "installment_no > 0 " +
        "AND emi_amount>=0 " +
        "AND principle_component >= 0 " +
        "AND interest_component >= 0 " +
                "AND version > 0"
)
public class Emi extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, name = "installment_no")
    private Integer installmentNo;

    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(name = "emi_amount", nullable = false)
    private Double emiAmount;

    @Column(nullable = false, name = "principle_component")
    private Double principleComponent;

    @Column(nullable = false, name = "interest_component")
    private Double interestComponent;

    @OneToOne
    @JoinColumn(name = "penalty_id")
    private Penalty penalty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmiStatus emiStatus;

    @Column(nullable = false, name = "version")
    private Integer version;

    @Column(nullable = false)
    private Boolean isActive=false;
}
