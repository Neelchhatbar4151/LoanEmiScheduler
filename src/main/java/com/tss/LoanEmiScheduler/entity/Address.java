package com.tss.LoanEmiScheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity{
    @Column(nullable = false)
    @NotBlank
    private String addressLine1;
    private String addressLine2;

    @Column(nullable = false)
    @NotBlank
    private String city;
    @Column(nullable = false)
    @NotBlank
    private String state;
    @Column(nullable = false)
    @NotBlank
    private String country;
    @Size(min = 6, max = 10)
    @Column(nullable = false, length = 10)
    @NotBlank
    private String postalCode;
}
