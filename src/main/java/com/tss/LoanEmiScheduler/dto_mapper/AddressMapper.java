package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.BorrowerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.response.AddressResponseDto;
import com.tss.LoanEmiScheduler.dto.response.UserDetailsFetchDto;
import com.tss.LoanEmiScheduler.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(OfficerSignUpRequestDto officerSignUpDto);
    Address toAddress(BorrowerSignUpRequestDto borrowerSignUpRequestDto);
    Address toAddress(BranchRequestDto branchRequestDto);
    Address toAddress(AddressResponseDto addressResponseDto);
}