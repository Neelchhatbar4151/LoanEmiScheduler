package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.FutureOrPresent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmiRepository extends JpaRepository<Emi, Long> {
    @Query("""
            SELECT e
            FROM Emi e
            WHERE e.loan.id = :loanId
              AND e.createdAt < :nextDate
              AND e.version = (
                  SELECT MAX(e2.version)
                  FROM Emi e2
                  WHERE e2.loan.id = e.loan.id
                    AND e2.installmentNo = e.installmentNo
                    AND e2.createdAt < :nextDate
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
            AND e.emiStatus NOT IN ('PAID', 'CANCELLED')
            AND e.dueDate < :today
            ORDER BY e.dueDate ASC
            """)
    List<Emi> findOverDueEmisByLoan(@Param("loan") Loan loan, @Param("today") LocalDate today);

    @Query(value = """
            SELECT * FROM emis e
            WHERE e.loan_id = :loanId
            AND e.is_active = true
            AND e.emi_status NOT IN ('CANCELLED')
            AND e.due_date >= :today
            ORDER BY e.due_date ASC
            LIMIT 1
           """, nativeQuery = true)
    List<Emi> findCurrentEmiByLoan(@Param("loanId") Long loanId, @Param("today") LocalDate today);

    @Query("""
           SELECT e FROM Emi e
           WHERE e.isActive = true
           AND e.emiStatus NOT IN ('PAID', 'CANCELLED')
           AND e.dueDate < :today
           """)
    List<Emi> findOverdueEmis(@Param("today") LocalDate today);

    @Query("""
           SELECT e FROM Emi e
           WHERE e.isActive = true
           AND e.emiStatus NOT IN ('PAID', 'CANCELLED')
           AND e.dueDate = :givenDate
           """)
    List<Emi> findUnpaidEmisWithGivenDueDate(@Param("givenDate") LocalDate givenDate);

//    future emis
    List<Emi> findEmiByLoanIdAndDueDateAfterOrderByDueDateAsc(Long loanId, @FutureOrPresent LocalDate dueDateAfter);
    Emi findFirstEmiByLoanIdAndDueDateAfterOrderByDueDateAsc(Long loanId, @FutureOrPresent LocalDate dueDateAfter);

//    past emis
    List<Emi> findEmiByLoanIdAndDueDateBeforeOrderByDueDate(Long loanId, @FutureOrPresent LocalDate dueDateBefore);
}
