package com.tss.LoanEmiScheduler.repository;

import com.tss.LoanEmiScheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
            SELECT u
            FROM User u
            LEFT JOIN Officer o ON u.id = o.id
            LEFT JOIN Borrower b ON u.id = b.id
            WHERE o.username = :identifier
               OR b.accountNumber = :identifier
            """)
    Optional<User> findByIdentifier(@Param("identifier")String identifier);
}
