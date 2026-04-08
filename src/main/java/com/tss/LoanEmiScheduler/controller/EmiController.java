package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.EmiRequestDto;
import com.tss.LoanEmiScheduler.dto.response.EmiResponseDto;
import com.tss.LoanEmiScheduler.service.EmiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans/emis")
@RequiredArgsConstructor
public class EmiController {
    private final EmiService emiService;

//    paginate
    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/future-emis")
    public List<EmiResponseDto> getFutureEmi(@RequestBody EmiRequestDto emiRequestDto){
        return emiService.getFutureEmiForLoan(emiRequestDto);
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/past-emis")
    public List<EmiResponseDto> getPastEmi(@RequestBody EmiRequestDto emiRequestDto){
        return emiService.getPastEmiForLoan(emiRequestDto);
    }

    @PreAuthorize("hasRole('BORROWER')")
    @GetMapping("/next-emi")
    public EmiResponseDto getNextEmi(@RequestBody EmiRequestDto emiRequestDto){
        return emiService.getNextEmiForLoan(emiRequestDto);
    }
}

