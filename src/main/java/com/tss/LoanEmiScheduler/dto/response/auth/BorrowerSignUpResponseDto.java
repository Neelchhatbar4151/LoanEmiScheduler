package com.tss.LoanEmiScheduler.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BorrowerSignUpResponseDto extends UserSignUpDto {
    //response after succ sign up
    private String accountNumber;
    private String email;
}
