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
    @NotBlank
    private String username;

    @NotNull
    private Long branchId;

    private final Role role = Role.OFFICER;
}
