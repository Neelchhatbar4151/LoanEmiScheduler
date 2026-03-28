package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

}
