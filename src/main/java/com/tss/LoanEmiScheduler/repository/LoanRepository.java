package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByBranchId(Long branchId);
    @Query("SELECT l FROM Loan l WHERE l.borrower.accountNumber = :accountNumber")
    List<Loan> findByBorrowerAccountNumber(@Param("accountNumber") String accountNumber);
    // Derived query: find loan where number matches AND borrower's account matches
    Optional<Loan> findByLoanNumberAndBorrowerAccountNumber(String loanNumber, String accountNumber);
    Optional<Loan> findByLoanNumber(String loanNumber);

}
