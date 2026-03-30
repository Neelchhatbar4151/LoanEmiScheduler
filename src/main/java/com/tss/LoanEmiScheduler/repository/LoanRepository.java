package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByBranchId(Long branchId);
}
