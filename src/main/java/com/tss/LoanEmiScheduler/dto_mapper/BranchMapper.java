package com.tss.LoanEmiScheduler.dto_mapper;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.entity.Branch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchMapper {
    Branch toBranch(BranchRequestDto branchRequestDto);
    BranchResponseDto toBranchResponseDto(Branch branch);
}
