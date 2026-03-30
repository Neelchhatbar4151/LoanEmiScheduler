package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long> {
    Optional<GlobalConfig> findByKey(String key);
}
