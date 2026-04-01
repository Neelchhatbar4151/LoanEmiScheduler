package com.tss.LoanEmiScheduler.cron_job;

import com.tss.LoanEmiScheduler.action_service.EmiActionService;
import com.tss.LoanEmiScheduler.action_service.LoanActionService;
import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Penalty;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.NotificationType;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.PenaltyRepository;
import com.tss.LoanEmiScheduler.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyEmiProcessingJob {
    private final EmiRepository emiRepo;
    private final PenaltyRepository penaltyRepository;
    private final EmiActionService emiActionService;
    private final LoanActionService loanActionService;
    private final NotificationService notificationService;

    private final LoanStrategyFactory loanStrategyFactory;

    public void run(){
        checkOverdue();
        checkSoonToBeOverdue();
    }

    private void checkOverdue(){
        List<Emi> overdueEmis = emiRepo.findOverdueEmis(LocalDate.now());

        for(Emi currEmi: overdueEmis){
            long dpd = ChronoUnit.DAYS.between(currEmi.getDueDate(), LocalDate.now());
            if(currEmi.getEmiStatus() != EmiStatus.OVERDUE){
                try{
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("emiAmount", currEmi.getEmiAmount());
                    variables.put("dueDate", currEmi.getDueDate());
                    variables.put("name", currEmi.getLoan().getBorrower().getFirstName());

                    notificationService.sendNotification(currEmi.getLoan().getBorrower().getEmail(), NotificationType.OVERDUE, variables);
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }

                Penalty penalty = new Penalty();
                penalty.setPenaltyAmount(GlobalConstant.PENALTY_AMOUNT);
                penalty.setRemainingAmount(GlobalConstant.PENALTY_AMOUNT);
                penaltyRepository.save(penalty);

                currEmi.setPenalty(penalty);

                loanStrategyFactory.getStrategy(
                        currEmi.getLoan()
                                .getLoanStrategy())
                        .reAmortize(currEmi);
            }

            if(dpd > 90){
                loanActionService.handleNpa(currEmi.getLoan());
            }
            else if(dpd > 60){
                loanActionService.handleDelinquent(currEmi.getLoan());
            }
            else{
                loanActionService.handleOverdue(currEmi.getLoan());
            }

            BigDecimal penalInterest = currEmi.getPenalInterest();
            BigDecimal remainingAmount = currEmi.getRemainingInterestComponent().add(currEmi.getRemainingPrincipalComponent());

            BigDecimal newPenalInterest = remainingAmount.multiply(
                    GlobalConstant.PENAL_INTEREST_RATE
                            .multiply(new BigDecimal(dpd/365)));

            BigDecimal difference = newPenalInterest.subtract(penalInterest);
            currEmi.setRemainingPenalInterest(
                    currEmi.getRemainingPenalInterest()
                            .add(difference)
            );
            emiActionService.handleOverdue(currEmi);
        }
    }

    private void checkSoonToBeOverdue(){
        List<Emi> soonToBeOverdueEmis = emiRepo.findUnpaidEmisWithGivenDueDate(LocalDate.now().plusDays(2));
        for(Emi currEmi : soonToBeOverdueEmis){
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("emiAmount", currEmi.getEmiAmount());
                variables.put("dueDate", currEmi.getDueDate());
                variables.put("name", currEmi.getLoan().getBorrower().getFirstName());

                notificationService.sendNotification(currEmi.getLoan().getBorrower().getEmail(), NotificationType.REMINDER, variables);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
