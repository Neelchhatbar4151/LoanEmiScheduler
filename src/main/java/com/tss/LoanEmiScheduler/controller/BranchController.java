package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;
    @PostMapping("/branches")
//    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<BranchResponseDto> save(@RequestBody @Valid BranchRequestDto branchRequestDto){
        BranchResponseDto branchResponseDto = branchService.save(branchRequestDto);
        //throw exp
        return ResponseEntity.ok(branchResponseDto);
    }

    @GetMapping("/branches")
    public ResponseEntity<List<BranchResponseDto>> findAll(){
        return ResponseEntity.ok(branchService.findAll());
    }
}
