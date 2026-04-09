package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Borrower;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

//    for officer
    Page<Loan> findByBranchIdAndLoanStatus(Long branchId, LoanStatus loanStatus, Pageable pageable);

    //    borrowers loans filter of status
    Page<Loan> findByLoanStatusAndBorrowerAccountNumber(LoanStatus loanStatus, String accountNumber, Pageable pageable);

//    all by borrower
    @Query("SELECT l FROM Loan l WHERE l.borrower.accountNumber = :accountNumber")
    Page<Loan> findByBorrowerAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

//    signle loan for borrower
    Optional<Loan> findByLoanNumberAndBorrowerAccountNumber(String loanNumber, String accountNumber);

//    to officer for checking
    Optional<Loan> findByLoanNumber(String loanNumber);

    Page<Loan> findByBranchIdAndBorrowerId(Long branchId, Long borrowerId, Pageable pageable);

//    officer view all loans of branch
    Page<Loan> findByBranchId(Long branchId, Pageable pageable);

//   limit 3
    @Query("""
           SELECT COUNT(l)
           FROM Loan l
           WHERE l.borrower.accountNumber = :accountNumber
           AND l.loanStatus NOT IN ('CLOSED', 'REJECTED')
           """)
    long countByBorrower(@Param("accountNumber") String accountNumber);
    Page<Loan> findByOfficerId(Long officerId, Pageable pageable);
}
