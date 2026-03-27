package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EmiMapper.class})
public interface LoanMapper {

    @Mapping(source = "borrower.accountNumber", target = "borrowerAccountNumber")
    @Mapping(source = "borrower.name", target = "borrowerAccountName")

    @Mapping(source = "approvedAt", target = "disbursementDate")

    @Mapping(source = "branch.name", target = "branch")

    @Mapping(source = "penalty.penaltyAmount", target = "penaltyAmount", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(source = "penalty.remainingAmount", target = "penaltyRemaining", defaultExpression = "java(java.math.BigDecimal.ZERO)")

    LoanResponseDto toDto(Loan loan);

    List<LoanResponseDto> toDtoList(List<Loan> loans);
}