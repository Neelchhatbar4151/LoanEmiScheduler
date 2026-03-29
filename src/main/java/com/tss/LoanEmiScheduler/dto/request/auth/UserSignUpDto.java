package com.tss.LoanEmiScheduler.dto.request.auth;

import com.tss.LoanEmiScheduler.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder
public class UserSignUpDto {
    //for sign up, this is base class

    @NotBlank
    @NotNull
    private String firstName;

    @NotBlank
    private String middleName;

    @NotBlank
    @NotNull
    private String lastName;

    @Pattern(regexp = "^(?:[6-9]\\d{9}|\\d{6,15})$") //internation support
    @NotBlank
    @NotNull
    private String phone;

    @Pattern(regexp = "^\\+[1-9]\\d{0,3}$")
    @NotBlank
    private String countryCode = "+91"; // for phone number, default +91

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$")
    @NotBlank
    @NotNull
    private String panCard;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$") //8 char, upper, lower, special, digit
    @NotBlank
    @NotNull
    private String password;

    @Past
    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private Gender gender;

    @Email
    @NotBlank
    @NotNull
    private String email;

    @NotBlank
    private String addressLine1;
    @NotBlank
    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @Size(min = 6, max = 10)
    @NotBlank
    private String postalCode;
}
