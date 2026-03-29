package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.AddressMapper;
import com.tss.LoanEmiScheduler.dto_mapper.BranchMapper;
import com.tss.LoanEmiScheduler.entity.Address;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final AddressMapper addressMapper;

    public BranchResponseDto save(BranchRequestDto branchRequestDto){
        Branch branch = branchMapper.toBranch(branchRequestDto);
        Address address = addressMapper.toAddress(branchRequestDto);

        branch.setAddress(address);
        return branchMapper.toBranchResponseDto(branchRepository.save(branch));
    }

    public List<BranchResponseDto> findAll(){
        return branchRepository.findAll()
                .stream().map(branchMapper::toBranchResponseDto).toList();
    }
}
