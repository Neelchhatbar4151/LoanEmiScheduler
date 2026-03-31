package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.PaymentAllocation;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    List<PaymentAllocation> findByEmiIdAndPaymentAllocationType(Long emiId, PaymentAllocationType paymentAllocationType);
    List<PaymentAllocation> findByEmiId(Long emiId);
}
