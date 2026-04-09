package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.response.EmiResponseDto;
import com.tss.LoanEmiScheduler.dto.response.FutureEmiResponseDto;
import com.tss.LoanEmiScheduler.entity.Emi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmiMapper {

    @Mapping(source = "penalty.penaltyAmount", target = "penaltyAmount", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(source = "penalty.remainingAmount", target = "penaltyRemaining", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(source = "loan.loanNumber", target = "loanNumber")
    EmiResponseDto toDto(Emi emi);

    @Mapping(source = "loan.loanNumber", target = "loanNumber")
    List<EmiResponseDto> toDtoList(List<Emi> emis);

    @Mapping(source = "loan.loanNumber", target = "loanNumber")
    FutureEmiResponseDto toFutureEmiResponseDto(Emi emi);
    default java.math.BigDecimal scale(java.math.BigDecimal value) {
        return value == null ? null : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}