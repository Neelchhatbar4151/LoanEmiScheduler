package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.ApproveRequestDto;
import com.tss.LoanEmiScheduler.dto.request.LoanApplyRequestDto;
import com.tss.LoanEmiScheduler.dto.request.RejectRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanApplyResponseDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.service.LoanService;
import com.tss.LoanEmiScheduler.service.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.LOAN;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {
    private final LoanService loanService;
    private final OfficerService officerService;

    @PreAuthorize("hasRole('BORROWER')")
    @PostMapping("/my-loans")
    public ResponseEntity<LoanApplyResponseDto> applyLoan(
            @RequestBody@Valid LoanApplyRequestDto loanApplyRequestDto
    ){
        log.info("{} Apply: Initializing application for {}", LOAN, loanApplyRequestDto);
        LoanApplyResponseDto loanApplyResponseDto = loanService.applyLoan(loanApplyRequestDto);
        log.info("{} Apply: Success for application {} of amount {} for {} months of {} loan type",
                LOAN,
                loanApplyResponseDto.getLoanNumber(),
                loanApplyResponseDto.getPrincipalAmount(),
                loanApplyResponseDto.getTenure(),
                loanApplyResponseDto.getLoanType()
        );
        return ResponseEntity.ok(loanApplyResponseDto);
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanResponseDto>> findLoanByBorrower(
            @RequestParam(required = false)LoanStatus status
    ) {
        if(status != null){
            return ResponseEntity.ok(loanService.findLoanByBorrowerWithStatus(status));
        }
        return ResponseEntity.ok(loanService.findLoanByBorrower());
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/my-loans/{loanNumber}")
    public ResponseEntity<LoanResponseDto> findLoanByLoanNumber(@PathVariable String loanNumber) {
        return ResponseEntity.ok(loanService.findLoanByLoanNumber(loanNumber));
    }

    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/branch-loans")
    public ResponseEntity<List<LoanResponseDto>> findLoanByBranch(
            @RequestParam(required = false)LoanStatus status
    ) {
        if(status == null){
            return ResponseEntity.ok(loanService.findLoanByBranchId());
        }
        return ResponseEntity.ok(officerService.getAllLoans(status));
    }

    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/branch-loans/my-loans")
    public ResponseEntity<List<LoanResponseDto>> findByOfficer(){
        return ResponseEntity.ok(officerService.getAllLoansByOfficer());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/branch-loans/{accountNumber}")
    public ResponseEntity<List<LoanResponseDto>> findLoanByBorrower(
            @PathVariable String accountNumber
    ){
        return ResponseEntity.ok(officerService.findLoanByBorrower(accountNumber));
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PatchMapping("/branch-loans/approve")
    public ResponseEntity<LoanResponseDto> approveLoan(@RequestBody@Valid ApproveRequestDto requestDto) {
        log.info("{} Approve: Initializing approval process for loan {}", LOAN, requestDto.getLoanNumber());
        LoanResponseDto loanResponseDto = officerService.approveLoan(requestDto);
        log.info("{} Approve: Success for approve for loan {}", LOAN, loanResponseDto.getLoanNumber());
        return ResponseEntity.ok(loanResponseDto);
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PatchMapping("/branch-loans/reject")
    public ResponseEntity<LoanResponseDto> rejectLoan(@RequestBody@Valid RejectRequestDto requestDto) {
        log.info("{} Reject: Initializing rejection process for loan {}", LOAN, requestDto.getLoanNumber());
        LoanResponseDto loanResponseDto = officerService.rejectLoan(requestDto);
        log.info("{} Reject: Success for reject for loan {}", LOAN, loanResponseDto.getLoanNumber());
        return ResponseEntity.ok(loanResponseDto);
    }
}
