package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.TransactionMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Table(name = "transactions")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "transaction_amount > 0")
@Where(clause = "is_deleted = false")
public class Transaction extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", updatable = false)
    private Loan loan;

    @Column(nullable = false, name = "transaction_amount", precision = 19, scale = 2, updatable = false)
    @Positive
    private BigDecimal transactionAmount;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransactionMode transactionMode;

    @Column(updatable = false)
    private String transactionReference;  //null for cash
}
