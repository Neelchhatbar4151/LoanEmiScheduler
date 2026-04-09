package com.tss.LoanEmiScheduler.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BorrowerSignUpResponseDto{
    //response after succ sign up
    private String accountNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
}
