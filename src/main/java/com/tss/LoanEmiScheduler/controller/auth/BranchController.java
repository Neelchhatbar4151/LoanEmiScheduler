package com.tss.LoanEmiScheduler.controller.auth;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;
    @PostMapping("/branch")
    public ResponseEntity<BranchResponseDto> save(@RequestBody @Valid BranchRequestDto branchRequestDto){
        BranchResponseDto branchResponseDto = branchService.save(branchRequestDto);
        //throw exp
        return ResponseEntity.ok(branchResponseDto);
    }
}
