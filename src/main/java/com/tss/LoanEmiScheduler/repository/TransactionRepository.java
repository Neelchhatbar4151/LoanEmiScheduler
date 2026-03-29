package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
