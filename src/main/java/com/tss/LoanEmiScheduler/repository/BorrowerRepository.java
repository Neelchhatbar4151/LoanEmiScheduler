package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
}
