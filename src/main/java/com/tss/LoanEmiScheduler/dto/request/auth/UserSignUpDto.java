package com.tss.LoanEmiScheduler.dto.request.auth;

import com.tss.LoanEmiScheduler.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder
public class UserSignUpDto {
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

    @Email
    @NotBlank
    @NotNull
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$") //8 char, upper, lower, special, digit
    @NotBlank
    @NotNull
    @ToString.Exclude
    private String password;
}
