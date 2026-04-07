package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.AddressMapper;
import com.tss.LoanEmiScheduler.dto_mapper.BranchMapper;
import com.tss.LoanEmiScheduler.entity.Address;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.BRANCH;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final AddressMapper addressMapper;

    public BranchResponseDto save(BranchRequestDto branchRequestDto){
        Branch branch = branchMapper.toBranch(branchRequestDto);
        log.info("{} Saving: Branch with name {} branch code {}", BRANCH, branch.getBranchName(), branch.getBranchCode());
        Address address = addressMapper.toAddress(branchRequestDto);
        branch.setAddress(address);
        branch = branchRepository.save(branch);
        log.info("{} Saving: Saved branch with branch name {} branch code {} with id {}", BRANCH, branch.getBranchName(), branch.getBranchCode(), branch.getId());
        return branchMapper.toBranchResponseDto(branch);
    }

    public List<BranchResponseDto> findAll(){
        return branchRepository.findAll()
                .stream().map(branchMapper::toBranchResponseDto).toList();
    }
}
