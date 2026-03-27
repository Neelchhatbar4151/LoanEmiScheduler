package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Table(name = "payment_allocations")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "amount_allocated > 0")
public class PaymentAllocation extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentAllocationType paymentAllocationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_id", nullable = false, updatable = false)
    private Emi emi;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amountAllocated;
}
