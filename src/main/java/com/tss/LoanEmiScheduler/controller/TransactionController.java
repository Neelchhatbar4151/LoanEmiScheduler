package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.TransactionRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BorrowerTransactionResponseDto;
import com.tss.LoanEmiScheduler.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.TRANSACTION;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    @PutMapping("/pay")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<String> payForLoan(@RequestBody @Valid TransactionRequestDto transactionRequestDto){
        log.info("{} Pay: Initialized payment for loan {} of amount {} using {}",
                TRANSACTION,
                transactionRequestDto.getLoanNumber(),
                transactionRequestDto.getTransactionAmount(),
                transactionRequestDto.getTransactionMode()
        );
        String message = transactionService.pay(transactionRequestDto);
        log.info("{} Pay: Success payment for loan {}",
                TRANSACTION,
                transactionRequestDto.getLoanNumber()
        );
        return ResponseEntity.ok(message);
    }
}
