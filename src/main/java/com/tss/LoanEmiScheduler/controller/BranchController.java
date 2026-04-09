package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.dto.request.BranchRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BranchResponseDto;
import com.tss.LoanEmiScheduler.enums.LogTag;
import com.tss.LoanEmiScheduler.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class BranchController {
    private final BranchService branchService;
    @PostMapping("/branches")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<BranchResponseDto> save(@RequestBody @Valid BranchRequestDto branchRequestDto){
        log.info("{} Insert: Initialized process to save new branch {}", LogTag.BRANCH.getValue(), branchRequestDto.getBranchCode());
        BranchResponseDto branchResponseDto = branchService.save(branchRequestDto);
        log.info("{} Insert: Success for new branch {}", LogTag.BRANCH.getValue(), branchRequestDto.getBranchCode());
        return ResponseEntity.ok(branchResponseDto);
    }

    @GetMapping("/branches")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<Page<BranchResponseDto>> findAll(
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(branchService.findAll(pageable));
    }
}
