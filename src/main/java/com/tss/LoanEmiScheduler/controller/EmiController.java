package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.EmiRequestDto;
import com.tss.LoanEmiScheduler.dto.response.EmiResponseDto;
import com.tss.LoanEmiScheduler.dto.response.FutureEmiResponseDto;
import com.tss.LoanEmiScheduler.service.EmiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans/emis")
@RequiredArgsConstructor
public class EmiController {
    private final EmiService emiService;

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/future-emis")
    public ResponseEntity<Page<FutureEmiResponseDto>> getFutureEmi(@RequestBody EmiRequestDto emiRequestDto, @PageableDefault(size = 5) Pageable pageable){
        return ResponseEntity.ok(emiService.getFutureEmiForLoan(emiRequestDto, pageable));
    }

//    paginate
    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/past-emis")
    public ResponseEntity<Page<EmiResponseDto>> getPastEmi(@RequestBody EmiRequestDto emiRequestDto, @PageableDefault(size = 5) Pageable pageable){
        return ResponseEntity.ok(emiService.getPastEmiForLoan(emiRequestDto, pageable));
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/next-emi")
    public ResponseEntity<FutureEmiResponseDto> getNextEmi(@RequestBody EmiRequestDto emiRequestDto){
        return ResponseEntity.ok(emiService.getNextEmiForLoan(emiRequestDto));
    }
}

