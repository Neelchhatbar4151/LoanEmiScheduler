package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Table(name = "penalties")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "penalty_amount > 0 AND " +
        "remaining_amount >= 0 AND " +
        "remaining_amount <= penalty_amount")
@Where(clause = "is_deleted = false")
public class Penalty extends BaseEntity {

    @Column(nullable = false,
            name = "penalty_amount",
            updatable = false,
            precision = 19,
            scale = 2)
    @Positive
    private BigDecimal penaltyAmount;

    @Column(nullable = false, name = "remaining_amount", precision = 19, scale = 2)
    @PositiveOrZero
    private BigDecimal remainingAmount;

    @PrePersist
    public void initRemainingAmount() {
        if (this.remainingAmount == null) {
            this.remainingAmount = this.penaltyAmount;
        }
    }
}
