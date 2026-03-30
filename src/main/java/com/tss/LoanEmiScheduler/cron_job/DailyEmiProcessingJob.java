package com.tss.LoanEmiScheduler.cron_job;

import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyEmiProcessingJob {
    private final EmiRepository emiRepo;

    public void run(){
        checkOverdue();
        checkSoonToBeOverdue();
    }

    private void checkOverdue(){
        List<Emi> overdueEmis ;
    }

    private void checkSoonToBeOverdue(){

    }
}
