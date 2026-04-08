package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.Officer;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {
    Optional<Officer> findByUsername(@NotBlank String username);
}
