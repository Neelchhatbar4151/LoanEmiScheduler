package com.tss.LoanEmiScheduler.entity;
import com.tss.LoanEmiScheduler.enums.Gender;
import com.tss.LoanEmiScheduler.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{
    @Column(nullable = false)
    @NotBlank
    private String firstName;

    @NotBlank
    private String middleName;

    @Column(nullable = false)
    @NotBlank
    private String lastName;

    @Column(nullable = false)
    @Pattern(regexp = "^(?:[6-9]\\d{9}|\\d{6,15})$") //internation support
    @NotBlank
    private String phone;

    @Column(nullable = false)
    @Pattern(regexp = "^\\+[1-9]\\d{0,3}$")
    @ColumnDefault("'+91'")
    @NotBlank
    private String countryCode = "+91"; // for phone number, default +91

    @Column(nullable = false)
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$")
    @NotBlank
    private String panCard;

    @Column(nullable = false)
    @NotBlank
    private String password;

    private LocalDateTime lastLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(nullable = false)
    @Past
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    @Email
    @NotBlank
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; //new
}
