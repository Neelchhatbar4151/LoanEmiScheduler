package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.request.TransactionRequestDto;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "loan", source = "loan")
    @Mapping(target = "transactionAmount", source = "dto.transactionAmount")
    @Mapping(target = "transactionMode", source = "dto.transactionMode")
    @Mapping(target = "transactionReference", source = "dto.transactionReference")
    Transaction toEntity(TransactionRequestDto dto, Loan loan);
}