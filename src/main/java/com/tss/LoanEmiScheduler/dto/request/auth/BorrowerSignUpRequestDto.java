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
//    sign up
    @NotBlank
    private String accountNumber;  //add method to generate

    @NotNull
    @PositiveOrZero
    private BigDecimal annualIncome;

    @NotNull
    @PositiveOrZero
    private BigDecimal debtAmount;

    private final Role role = Role.BORROWER;
    //annual income and debt amount will fetched from custom api in future, and will be removed from this dto
}
