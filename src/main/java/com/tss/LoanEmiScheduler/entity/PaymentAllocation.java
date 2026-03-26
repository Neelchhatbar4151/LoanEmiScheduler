package com.tss.LoanEmiScheduler.entity;

import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "payment_allocations")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAllocation extends BaseEntity{
    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAllocationType paymentAllocationType;

    @ManyToOne
    @JoinColumn(name = "emi_id")
    private Emi emi;

    @Column(nullable = false)
    private Double amountAllocated;
}
