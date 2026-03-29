package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {
}
