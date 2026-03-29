package com.tss.LoanEmiScheduler.dto.request.auth;

import com.tss.LoanEmiScheduler.enums.Gender;
import com.tss.LoanEmiScheduler.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder
public class OfficerSignUpRequestDto extends UserSignUpDto{
    @NotNull
    private String username;

    @NotNull
    private Long branchId;

    @NotNull
    @NotBlank
    private String addressLine1;
    @NotBlank
    private String addressLine2;

    @NotNull
    @NotBlank
    private String city;

    @NotNull
    @NotBlank
    private String state;

    @NotNull
    @NotBlank
    private String country;

    @Size(min = 6, max = 10)
    @NotNull
    @NotBlank
    private String postalCode;

    private final Role role = Role.OFFICER;
}
