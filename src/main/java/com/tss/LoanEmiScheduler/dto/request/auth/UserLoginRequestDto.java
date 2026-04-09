package com.tss.LoanEmiScheduler.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginRequestDto {
    @NotBlank
    private String identifier;
    @NotBlank
    @ToString.Exclude
    private String password;
}
