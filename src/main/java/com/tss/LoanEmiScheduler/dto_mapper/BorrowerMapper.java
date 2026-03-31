package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.request.auth.BorrowerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.response.UserDetailsFetchDto;
import com.tss.LoanEmiScheduler.dto.response.auth.BorrowerSignUpResponseDto;
import com.tss.LoanEmiScheduler.entity.Borrower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowerMapper {
    @Mapping(target = "password", ignore = true)
    Borrower toBorrower(BorrowerSignUpRequestDto signUpRequestDto);
    BorrowerSignUpResponseDto toBorrowerSignResponseDto(Borrower borrower);
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "panCard", source = "userDetailsFetchDto.panCard")
    Borrower toBorrower(UserDetailsFetchDto userDetailsFetchDto, BorrowerSignUpRequestDto borrowerSignUpRequestDto);
}
