package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String branchName;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String branchCode;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_id", unique = true, nullable = false)
    private Address address;
}
