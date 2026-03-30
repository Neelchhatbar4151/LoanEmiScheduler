package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.dto.request.auth.BorrowerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.UserLoginRequestDto;
import com.tss.LoanEmiScheduler.dto.response.auth.BorrowerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.AddressMapper;
import com.tss.LoanEmiScheduler.dto_mapper.BorrowerMapper;
import com.tss.LoanEmiScheduler.dto_mapper.OfficerMapper;
import com.tss.LoanEmiScheduler.dto_mapper.UserMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OfficerRepository officerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BranchRepository branchRepository;
    private final BorrowerRepository borrowerRepository;
    private final GlobalConfigRepository globalConfigRepository;
    private final OfficerMapper officerMapper;
    private final UserMapper userMapper;
    private final BorrowerMapper borrowerMapper;
    private final AddressMapper addressMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private static Long accountNumberCounter;

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

    @Transactional
    public BorrowerSignUpResponseDto register(BorrowerSignUpRequestDto borrowerSignUpRequestDto){
        Borrower borrower = borrowerMapper.toBorrower(borrowerSignUpRequestDto);
        Address address = addressMapper.toAddress(borrowerSignUpRequestDto);
        Branch branch = branchRepository.findById(borrowerSignUpRequestDto.getBranchId())
                .orElseThrow();

        address = addressRepository.save(address);
        borrower.setAddress(address);
        borrower.setPassword(encoder.encode(borrowerSignUpRequestDto.getPassword()));
        borrower.setBranch(branch);
        borrower.setAccountNumber(generateAccountNumber());

        borrower = borrowerRepository.save(borrower);

        return borrowerMapper.toBorrowerSignResponseDto(borrower);
    }

    public String verify(UserLoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getIdentifier(),
                        loginRequestDto.getPassword())
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authentication);
        }
        throw new BadCredentialsException("Bad credentials");
    }

    private String generateAccountNumber(){
        return "AC"+accountNumberCounter++;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAccountNumberCounter(){
        accountNumberCounter = Long.valueOf(
                globalConfigRepository
                        .findByKey(GlobalConstant.ACCOUNT_NUMBER_COUNTER_KEY)
                        .orElseThrow()
                        .getValue()
        );
    }

    @EventListener(ContextClosedEvent.class)
    public void runBeforeShutdown() {
        globalConfigRepository.save(
                new GlobalConfig(
                        GlobalConstant.ACCOUNT_NUMBER_COUNTER_KEY,
                        accountNumberCounter.toString()
                )
        );
    }
}
