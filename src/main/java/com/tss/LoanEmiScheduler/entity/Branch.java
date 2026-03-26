package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "branches")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Branch extends BaseEntity{
    @Column(nullable = false)
    private String branchName;
    @Column(nullable = false, unique = true)
    private String branchCode;
    @OneToOne
    @JoinColumn(name = "address_id", unique = true)
    private Address address;
}
