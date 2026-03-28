package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
