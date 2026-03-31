package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.response.UserDetailsFetchDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.entity.Officer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfficerMapper {
    @Mapping(target = "password", ignore = true)
    Officer toOfficer(OfficerSignUpRequestDto officerSignUpDto);
    OfficerSignUpResponseDto toOfficerSignUpResponseDto(Officer officer);
    @Mapping(source = "userDetailsFetchDto.panCard", target = "panCard")
    Officer toOfficer(UserDetailsFetchDto userDetailsFetchDto, OfficerSignUpRequestDto officerSignUpRequestDto);
}
