package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "officers")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Officer extends User{
    @Column(nullable = false, unique = true, updatable = false)
    private String username;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
