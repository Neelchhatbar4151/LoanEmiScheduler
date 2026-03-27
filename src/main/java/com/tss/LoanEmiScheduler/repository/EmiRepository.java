package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Emi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
                    AND e2.installmentNumber = e.installmentNumber
                    AND e2.createdAt < :nextDate
                    AND e2.isDeleted = false
              )
            """)
    List<Emi> findEmiScheduleAsOfDate(
            @Param("loanId") Long loanId,
            @Param("nextDate") LocalDateTime nextDate
    );
}
