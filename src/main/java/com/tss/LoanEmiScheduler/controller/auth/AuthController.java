package com.tss.LoanEmiScheduler.controller.auth;

import com.tss.LoanEmiScheduler.dto.request.auth.OfficerSignUpRequestDto;
import com.tss.LoanEmiScheduler.dto.response.auth.OfficerSignUpResponseDto;
import com.tss.LoanEmiScheduler.exception.SignUpFailedException;
import com.tss.LoanEmiScheduler.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/officer")
    public ResponseEntity<OfficerSignUpResponseDto> register(@RequestBody@Valid OfficerSignUpRequestDto officerSignUpDto){
        OfficerSignUpResponseDto officerSignUpResponseDto = authService.register(officerSignUpDto);
        if(officerSignUpResponseDto == null){
            throw new SignUpFailedException("Signup failure.");
        }
        return ResponseEntity.ok(officerSignUpResponseDto);
    }
    @GetMapping
    public String get(){
        return "yes";
    }
}
