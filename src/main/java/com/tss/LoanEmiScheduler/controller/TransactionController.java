package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.TransactionRequestDto;
import com.tss.LoanEmiScheduler.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    @PutMapping("/pay")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<String> payForLoan(@RequestBody @Valid TransactionRequestDto transactionRequestDto){
         return ResponseEntity.ok(transactionService.pay(transactionRequestDto));
    }
}
