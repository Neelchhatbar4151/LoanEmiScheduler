package com.tss.LoanEmiScheduler.dto.request.auth;

import com.tss.LoanEmiScheduler.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder
public class BorrowerSignUpRequestDto extends UserSignUpDto{
    @NotBlank
    private String branchCode;
}
