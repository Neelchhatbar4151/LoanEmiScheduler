package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EmiRepository extends JpaRepository<Emi, Long> {
    @Query("""
            SELECT e
            FROM Emi e
            WHERE e.loan.id = :loanId
              AND e.createdAt < :nextDate
              AND e.isDeleted = false
              AND e.version = (
                  SELECT MAX(e2.version)
                  FROM Emi e2
                  WHERE e2.loan.id = e.loan.id
                    AND e2.installmentNo = e.installmentNo
                    AND e2.createdAt < :nextDate
                    AND e2.isDeleted = false
              )
            """)
    List<Emi> findEmiScheduleAsOfDate(
            @Param("loanId") Long loanId,
            @Param("nextDate") LocalDateTime nextDate
    );

    boolean existsByLoanId(Long loanId);

    List<Emi> findByLoanIdAndIsActive(Long loanId, boolean isActive);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e FROM Emi e
            WHERE e.loan = :loan
            AND e.isActive = true
            AND e.emiStatus <> 'PAID'
            AND e.emiStatus <> 'CANCELLED'
            AND e.isDeleted = false
            AND (
                YEAR(e.dueDate) < YEAR(:today)
                OR (YEAR(e.dueDate) = YEAR(:today) AND MONTH(e.dueDate) <= MONTH(:today))
            )
            ORDER BY e.dueDate ASC
            """)
    List<Emi> findEligibleEmisForPayment(Loan loan, LocalDate today);
}
