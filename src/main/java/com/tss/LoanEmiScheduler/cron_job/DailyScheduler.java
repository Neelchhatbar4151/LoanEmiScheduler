package com.tss.LoanEmiScheduler.cron_job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyScheduler {
    private final DailyEmiProcessingJob job;

    @Scheduled(cron = "0 0 0 * * ?")
    public void processEmi(){
        job.run();
    }
}
