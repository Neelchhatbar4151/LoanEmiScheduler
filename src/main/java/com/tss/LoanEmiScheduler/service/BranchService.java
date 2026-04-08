package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.AddressMapper;
import com.tss.LoanEmiScheduler.dto_mapper.BranchMapper;
import com.tss.LoanEmiScheduler.entity.Address;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.enums.LogTag;
import com.tss.LoanEmiScheduler.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final AddressMapper addressMapper;

    public BranchResponseDto save(BranchRequestDto branchRequestDto){
        Branch branch = branchMapper.toBranch(branchRequestDto);
        log.info("{} Saving: Branch with name {} branch code {}", LogTag.BRANCH.getValue(), branch.getBranchName(), branch.getBranchCode());
        Address address = addressMapper.toAddress(branchRequestDto);
        branch.setAddress(address);
        branch = branchRepository.save(branch);
        log.info("{} Saving: Saved branch with branch name {} branch code {} with id {}", LogTag.BRANCH.getValue(), branch.getBranchName(), branch.getBranchCode(), branch.getId());
        return branchMapper.toBranchResponseDto(branch);
    }

    public Page<BranchResponseDto> findAll(Pageable pageable){
        return branchRepository.findAll(pageable)
                .map(branchMapper::toBranchResponseDto);
    }
}
