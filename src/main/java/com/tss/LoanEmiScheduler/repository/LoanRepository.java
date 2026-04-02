package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Borrower;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

//    for officer
    List<Loan> findByBranchIdAndLoanStatus(Long branchId, LoanStatus loanStatus);

    //    borrowers loans filter of status
    List<Loan> findByLoanStatusAndBorrowerAccountNumber(LoanStatus loanStatus, String accountNumber);

//    all by borrower
    @Query("SELECT l FROM Loan l WHERE l.borrower.accountNumber = :accountNumber")
    List<Loan> findByBorrowerAccountNumber(@Param("accountNumber") String accountNumber);

//    signle loan for borrower
    Optional<Loan> findByLoanNumberAndBorrowerAccountNumber(String loanNumber, String accountNumber);

//    to officer for checking
    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByBranchIdAndBorrowerId(Long branchId, Long borrowerId);

//    officer view all loans of branch
    List<Loan> findByBranchId(Long branchId);

//   limit 3
    @Query("""
           SELECT COUNT(l)
           FROM Loan l
           WHERE l.borrower.accountNumber = :accountNumber
           AND l.loanStatus NOT IN ('CLOSED', 'REJECTED')
           """)
    long countByBorrower(@Param("accountNumber") String accountNumber);
    List<Loan> findByOfficerId(Long officerId);
}
