package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.response.EmiResponseDto;
import com.tss.LoanEmiScheduler.entity.Emi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmiMapper {

    @Mapping(source = "loan.id", target = "loanId")
    @Mapping(source = "penalty.penaltyAmount", target = "penaltyAmount", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(source = "penalty.remainingAmount", target = "penaltyRemaining", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    EmiResponseDto toDto(Emi emi);

    List<EmiResponseDto> toDtoList(List<Emi> emis);
}