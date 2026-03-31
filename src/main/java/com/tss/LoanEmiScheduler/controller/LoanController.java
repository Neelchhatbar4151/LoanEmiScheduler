package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.LoanApplyRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanApplyResponseDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.service.LoanService;
import com.tss.LoanEmiScheduler.service.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;
    private final OfficerService officerService;

    @PreAuthorize("hasRole('BORROWER')")
    @PostMapping("/my-loans")
    public ResponseEntity<LoanApplyResponseDto> applyLoan(@RequestBody@Valid LoanApplyRequestDto loanApplyRequestDto) throws BadRequestException {
        LoanApplyResponseDto loanApplyResponseDto = loanService.applyLoan(loanApplyRequestDto);
        if(loanApplyResponseDto == null)
            throw new BadRequestException("Loan application failed.");
        return ResponseEntity.ok(loanApplyResponseDto);
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanResponseDto>> findLoanByBorrower() {
        return ResponseEntity.ok(loanService.findLoanByBorrower());
    }

//    @PreAuthorize("hasRole('OFFICER')")
//    @GetMapping("/branch-loans")
//    public ResponseEntity<List<LoanResponseDto>> findLoanByBranch() {
//        return ResponseEntity.ok(officerService.getPendingLoans();
//    }

//    @PreAuthorize("hasRole('OFFICER')")
//    @PatchMapping("/status")
//    public ResponseEntity<List<LoanResponseDto>> updateLoanStatus() {
////        return ResponseEntity.ok(officerService.approveLoan());
//    }



    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/loans/{loanNumber}")
    public ResponseEntity<LoanResponseDto> findLoanByLoanNumber(@PathVariable String loanNumber) {
        return ResponseEntity.ok(loanService.findLoanByLoanNumber(loanNumber));
    }
}
