package com.tss.LoanEmiScheduler.dto.response;

import com.tss.LoanEmiScheduler.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class UserDetailsFetchDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private String panCard;
    private AddressResponseDto addressResponseDto;
    private LocalDate dateOfBirth;
    private Gender gender;
    private BigDecimal annualIncome;
    private BigDecimal debtAmount;
}
