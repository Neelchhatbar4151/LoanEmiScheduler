package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.dto.request.auth.BorrowerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.UserLoginRequestDto;
import com.tss.LoanEmiScheduler.dto.response.UserDetailsFetchDto;
import com.tss.LoanEmiScheduler.dto.response.auth.BorrowerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.AddressMapper;
import com.tss.LoanEmiScheduler.dto_mapper.BorrowerMapper;
import com.tss.LoanEmiScheduler.dto_mapper.OfficerMapper;
import com.tss.LoanEmiScheduler.dto_mapper.UserMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.AUTH;


@Slf4j
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
    private final PanValidationService panValidationService;

    private static Long accountNumberCounter;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6);

    @Transactional
    public OfficerSignUpResponseDto register(OfficerSignUpRequestDto officerSignUpDto){
        log.info("{} Register: for officer {} in branch {} with pan {} using email {}",
                AUTH,
                officerSignUpDto.getUsername(),
                officerSignUpDto.getBranchCode(),
                officerSignUpDto.getPanCard(),
                officerSignUpDto.getEmail()
        );
        UserDetailsFetchDto userDetailsFetchDto = panValidationService.fetchDetailsFromExternalSystem(officerSignUpDto.getPanCard());
        if(userDetailsFetchDto == null) {
            throw new ResourceNotFoundException(officerSignUpDto.getPanCard());
        }
        Officer officer = officerMapper.toOfficer(userDetailsFetchDto, officerSignUpDto);
        Address address = addressMapper.toAddress(userDetailsFetchDto.getAddressResponseDto());
        Branch branch = branchRepository.findByBranchCode(officerSignUpDto.getBranchCode())
                .orElseThrow();

        address = addressRepository.save(address);
        officer.setUsername(officerSignUpDto.getUsername());
        officer.setAddress(address);
        officer.setPassword(encoder.encode(officerSignUpDto.getPassword()));
        officer.setBranch(branch);
        officer.setRole(Role.OFFICER);

        officer = officerRepository.save(officer);
        log.info("{} Register: officer registered with username {} for branch code {} with address id {}",
                AUTH,
                officerSignUpDto.getUsername(),
                branch.getBranchCode(),
                address.getId()
        );
        return officerMapper.toOfficerSignUpResponseDto(officer);
    }

    @Transactional
    public BorrowerSignUpResponseDto register(BorrowerSignUpRequestDto borrowerSignUpRequestDto){
        log.info("{} Register: for borrower in branch {} with pan {} using email {}",
                AUTH,
                borrowerSignUpRequestDto.getBranchCode(),
                borrowerSignUpRequestDto.getPanCard(),
                borrowerSignUpRequestDto.getEmail()
        );
        UserDetailsFetchDto userDetailsFetchDto = panValidationService
                .fetchDetailsFromExternalSystem(
                        borrowerSignUpRequestDto.getPanCard()
                );
        log.info("{} Register: User fetched from gov DB: {} {} {}",
                AUTH,
                userDetailsFetchDto.getFirstName(),
                userDetailsFetchDto.getMiddleName(),
                userDetailsFetchDto.getLastName()
        );

        Borrower borrower = borrowerMapper.toBorrower(userDetailsFetchDto, borrowerSignUpRequestDto);
        Address address = addressMapper.toAddress(userDetailsFetchDto.getAddressResponseDto());
        Branch branch = branchRepository.findByBranchCode(borrowerSignUpRequestDto.getBranchCode())
                .orElseThrow();

        address = addressRepository.save(address);
        borrower.setAddress(address);
        borrower.setPassword(encoder.encode(borrowerSignUpRequestDto.getPassword()));
        borrower.setBranch(branch);
        borrower.setAccountNumber(generateAccountNumber());
        borrower.setRole(Role.BORROWER);

        borrower = borrowerRepository.save(borrower);
        log.info("{} Register: borrower registered with account number {} for branch code {} with address id {}",
                AUTH,
                borrower.getAccountNumber(),
                branch.getBranchCode(),
                address.getId()
        );
        return borrowerMapper.toBorrowerSignResponseDto(borrower);
    }

    public String verify(UserLoginRequestDto loginRequestDto) {
        log.info("{} Login: for user {}",
                AUTH,
                loginRequestDto.getIdentifier()
        );
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getIdentifier(),
                        loginRequestDto.getPassword())
        );
        log.info("{} Login: authentication done for user {}",
                AUTH,
                loginRequestDto.getIdentifier()
        );
        return jwtService.generateToken(authentication);
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
