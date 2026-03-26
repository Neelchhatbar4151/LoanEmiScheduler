package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Table(name = "penalties")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "penalty_amount > 0 AND remaining_amount >= 0")
public class Penalty extends BaseEntity {
    @Column(nullable = false, name = "penalty_amount")
    @Positive
    private Double penaltyAmount;

    @Column(nullable = false, name = "remaining_amount")
    @PositiveOrZero
    private Double remainingAmount;
}
