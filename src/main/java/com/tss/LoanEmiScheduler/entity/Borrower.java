package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Table(name = "borrowers")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "debt_amount >= 0 AND annual_income >= 0")
public class Borrower extends User{
    @Column(nullable = false, unique = true, updatable = false)
    private String accountNumber;  //add method to generate

    @Column(nullable = false, name = "annual_income")
    @PositiveOrZero
    private BigDecimal annualIncome;

    @Column(nullable = false, name = "debt_amount")
    @PositiveOrZero
    private BigDecimal debtAmount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer failedLoginAttempts;

    @Column(nullable = false)
    private Boolean accountLocked;
}
