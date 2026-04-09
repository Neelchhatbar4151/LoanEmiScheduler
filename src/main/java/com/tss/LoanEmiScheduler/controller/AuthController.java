package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.auth.BorrowerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.UserLoginRequestDto;
import com.tss.LoanEmiScheduler.dto.response.auth.BorrowerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.enums.LogTag;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.SignUpFailedException;
import com.tss.LoanEmiScheduler.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.protocol.IMAPProtocol;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/officer")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<OfficerSignUpResponseDto> register(@RequestBody@Valid OfficerSignUpRequestDto officerSignUpDto){
        log.info("{} Signup: Initiating registration for Officer: {}", LogTag.AUTH.getValue(), officerSignUpDto.getEmail());
        OfficerSignUpResponseDto officerSignUpResponseDto = authService.register(officerSignUpDto);
        log.info("{} Signup: SUCCESS for Officer: {}", LogTag.AUTH.getValue(), officerSignUpDto.getEmail());
        return ResponseEntity.ok(officerSignUpResponseDto);
    }
    @PostMapping("/signup/borrower")
    public ResponseEntity<BorrowerSignUpResponseDto> register(@RequestBody@Valid BorrowerSignUpRequestDto borrowerSignUpRequestDto){
        log.info("{} Signup: Initiating registration for Borrower: {}", LogTag.AUTH.getValue(), borrowerSignUpRequestDto.getEmail());
        BorrowerSignUpResponseDto borrowerSignUpResponseDto = authService.register(borrowerSignUpRequestDto);
        log.info("{} Signup: SUCCESS for Borrower: {}", LogTag.AUTH.getValue(),borrowerSignUpResponseDto.getEmail());
        return ResponseEntity.ok(borrowerSignUpResponseDto);
    }

//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody UserLoginRequestDto userLoginRequestDto){
//        log.info("{} Login: Initiating log in for user {}", LogTag.AUTH.getValue(), userLoginRequestDto.getIdentifier());
//        String message = authService.verify(userLoginRequestDto);
//        log.info("{} Login: SUCCESS for user {}", LogTag.AUTH.getValue(), userLoginRequestDto.getIdentifier());
//        return ResponseEntity.ok(message);
//    }

    @PostMapping("/login/officer")
    public ResponseEntity<String> loginOfficer(@RequestBody UserLoginRequestDto userLoginRequestDto){
        log.info("{} Login: Initiating log in for officer {}", LogTag.AUTH.getValue(), userLoginRequestDto.getIdentifier());
        String message = authService.verify(userLoginRequestDto, Role.OFFICER);
        log.info("{} Login: SUCCESS for officer {}", LogTag.AUTH.getValue(), userLoginRequestDto.getIdentifier());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login/borrower")
    public ResponseEntity<String> loginBorrower(@RequestBody UserLoginRequestDto userLoginRequestDto){
        log.info("{} Login: Initiating log in for borrower {}", LogTag.AUTH.getValue(), userLoginRequestDto.getIdentifier());
        String message = authService.verify(userLoginRequestDto, Role.BORROWER);
        log.info("{} Login: SUCCESS for borrower {}", LogTag.AUTH.getValue(), userLoginRequestDto.getIdentifier());
        return ResponseEntity.ok(message);
    }
}
