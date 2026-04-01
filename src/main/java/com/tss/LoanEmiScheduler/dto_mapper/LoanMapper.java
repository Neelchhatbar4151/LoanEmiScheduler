package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.request.LoanApplyRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanApplyResponseDto;
import com.tss.LoanEmiScheduler.dto.response.LoanAuditDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EmiMapper.class})
public interface LoanMapper {

    @Mapping(source = "borrower.accountNumber", target = "borrowerAccountNumber")
    @Mapping(source = "borrower.firstName", target = "borrowerAccountName")

    @Mapping(source = "approvedAt", target = "disbursementDate")

    @Mapping(source = "branch.branchName", target = "branch")

    @Mapping(source = "penalty.penaltyAmount", target = "penaltyAmount", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(source = "penalty.remainingAmount", target = "penaltyRemaining", defaultExpression = "java(java.math.BigDecimal.ZERO)")

    LoanResponseDto toDto(Loan loan);
    List<LoanResponseDto> toDtoList(List<Loan> loans);

    Loan toLoan(LoanApplyRequestDto loanApplyRequestDto);

    @Mapping(source = "loanStatus", target = "status")
    LoanApplyResponseDto toLoanApplyResponseDto(Loan loan);

    // MapStruct automatically generates the loop for the List version
    List<LoanAuditDto> toAuditDtoList(List<Loan> loans);

    // Define how a single snapshot looks
    LoanAuditDto toAuditDto(Loan loan);
}