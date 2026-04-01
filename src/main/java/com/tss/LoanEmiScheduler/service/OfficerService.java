package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.action_service.LoanActionService;
import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.dto.request.ApproveRequestDto;
import com.tss.LoanEmiScheduler.dto.request.RejectRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficerService {
    private final LoanRepository loanRepo;
    private final UserRepository userRepository;
    private final EmiRepository emiRepository;

    private final LoanStrategyFactory strategyFactory;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    private final NotificationService notificationService;

    private final LoanActionService loanActionService;
    private final StrategySuggestionService strategySuggestionService;

    public List<LoanResponseDto> getPendingLoans(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String officerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(officerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not an officer.");
        }

        Officer officer = ((Officer) user);

        List<Loan> pendingLoansForOfficer = loanRepo.findByBranchIdAndLoanStatus(officer.getBranch().getId(), LoanStatus.APPLIED);
        List<LoanResponseDto> dtos = new ArrayList<>();
        for (Loan loan : pendingLoansForOfficer) {
            LoanResponseDto dto = loanMapper.toDto(loan);
            dto.setSuggestedStrategy(strategySuggestionService.getSuggestedStrategy(loan));
            dtos.add(dto);
        }
        return dtos;
    }

    private void checkIfEligible(Loan loan, Officer officer){
        if(loan.getLoanStatus() != LoanStatus.APPLIED){
            throw new UnsupportedOperationException("Loan is not in Application stage.");
        }

        Branch officerBranch = officer.getBranch();
        Branch loanBranch = loan.getBranch();

        if(officerBranch == null || loanBranch == null){
            throw new IllegalStateException("Branch field is not set in Either officer or loan entity.");
        }

        if(!officerBranch.getBranchCode().equals(loanBranch.getBranchCode())){
            throw new UnsupportedOperationException("Loan from different branch can't be approved.");
        }
    }

    @Transactional
    public LoanResponseDto approveLoan(ApproveRequestDto request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String officerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(officerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not an officer.");
        }
        Officer officer = ((Officer) user);
        Loan loan = loanRepo.findByLoanNumber(request.getLoanNumber()).orElseThrow(() -> new ResourceNotFoundException("Loan"));
        checkIfEligible(loan, officer);

        //When applying loan application this will get set.
//        loan.setInterestRate(GlobalConstant.INTEREST_RATE);

        loan.setApprovedAt(LocalDateTime.now());
        loan.setOfficer(officer);
        List<Emi> schedule = strategyFactory.getStrategy(request.getLoanStrategy()).generateSchedule(loan);

        emiRepository.saveAll(schedule);

        LoanResponseDto dto = loanMapper.toDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));
        loan.setLoanStrategy(request.getLoanStrategy());
        dto.setLoanStrategy(request.getLoanStrategy());

        loanActionService.handleActive(loan);
//
//        try {
//            notificationService.sendNotification(loan.getBorrower().getEmail(), "Loan Approved", "Congratulations Your loan with Loan Number: " + loan.getLoanNumber() + " has been approved.");
//        }
//        catch(Exception e){
//            throw new RuntimeException(e);
//        }
        return dto;
    }

    @Transactional
    public LoanResponseDto rejectLoan(RejectRequestDto requestDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String officerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(officerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not an officer.");
        }
        Officer officer = ((Officer) user);
        Loan loan = loanRepo.findByLoanNumber(requestDto.getLoanNumber()).orElseThrow(()->new ResourceNotFoundException("Loan"));
        loan.setOfficer(officer);
        checkIfEligible(loan, officer);

        loanActionService.handleRejected(loan);

        return loanMapper.toDto(loan);
    }
}
