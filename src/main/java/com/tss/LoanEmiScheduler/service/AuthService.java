package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.AddressMapper;
import com.tss.LoanEmiScheduler.dto_mapper.OfficerMapper;
import com.tss.LoanEmiScheduler.dto_mapper.UserMapper;
import com.tss.LoanEmiScheduler.entity.Address;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.entity.Officer;
import com.tss.LoanEmiScheduler.entity.User;
import com.tss.LoanEmiScheduler.repository.AddressRepository;
import com.tss.LoanEmiScheduler.repository.BranchRepository;
import com.tss.LoanEmiScheduler.repository.OfficerRepository;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OfficerRepository officerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BranchRepository branchRepository;
    private final OfficerMapper officerMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6);

    @Transactional
    public OfficerSignUpResponseDto register(OfficerSignUpRequestDto officerSignUpDto){
        Officer officer = officerMapper.toOfficer(officerSignUpDto);
        Address address = addressMapper.toAddress(officerSignUpDto);
        Branch branch = branchRepository.findById(officerSignUpDto.getBranchId())
                .orElseThrow();

        address = addressRepository.save(address);
        officer.setAddress(address);
        officer.setPassword(encoder.encode(officerSignUpDto.getPassword()));
        officer.setBranch(branch);

        officer = officerRepository.save(officer);

        return officerMapper.toOfficerSignUpResponseDto(officer);
    }
}
