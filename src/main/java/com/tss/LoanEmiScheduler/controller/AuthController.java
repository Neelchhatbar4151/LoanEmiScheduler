package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.auth.BorrowerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.request.auth.UserLoginRequestDto;
import com.tss.LoanEmiScheduler.dto.response.auth.BorrowerSignUpResponseDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.exception.SignUpFailedException;
import com.tss.LoanEmiScheduler.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/officer")
    public ResponseEntity<OfficerSignUpResponseDto> register(@RequestBody@Valid OfficerSignUpRequestDto officerSignUpDto){
        OfficerSignUpResponseDto officerSignUpResponseDto = authService.register(officerSignUpDto);
        if(officerSignUpResponseDto == null){
            throw new SignUpFailedException("Officer.");
        }
        return ResponseEntity.ok(officerSignUpResponseDto);
    }
    @PostMapping("/signup/borrower")
    public ResponseEntity<BorrowerSignUpResponseDto> register(@RequestBody@Valid BorrowerSignUpRequestDto borrowerSignUpRequestDto){
        BorrowerSignUpResponseDto borrowerSignUpResponseDto = authService.register(borrowerSignUpRequestDto);
        if(borrowerSignUpResponseDto == null)
            throw new SignUpFailedException("Borrower.");
        return ResponseEntity.ok(borrowerSignUpResponseDto);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserLoginRequestDto userLoginRequestDto){
        return authService.verify(userLoginRequestDto);
    }
}
