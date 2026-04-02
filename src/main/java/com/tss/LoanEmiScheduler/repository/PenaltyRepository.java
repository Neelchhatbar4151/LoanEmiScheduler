package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
}
