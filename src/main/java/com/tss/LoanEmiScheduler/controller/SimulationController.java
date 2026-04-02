package com.tss.LoanEmiScheduler.controller;

import com.tss.LoanEmiScheduler.cron_job.DailyEmiProcessingJob;
import com.tss.LoanEmiScheduler.cron_job.DailyScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulate")
@RequiredArgsConstructor
public class SimulationController {

    private final DailyEmiProcessingJob job;

    @GetMapping("/job")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<String> simulateJob(){
        job.run();
        return ResponseEntity.ok("Job Complete.");
    }
}
