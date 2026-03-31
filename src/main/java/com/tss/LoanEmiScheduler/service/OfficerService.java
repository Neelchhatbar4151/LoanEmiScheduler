package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.action_service.LoanActionService;
import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.dto.request.ApproveRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Branch;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.Officer;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final LoanRepository loanRepo;

    private final LoanStrategyFactory strategyFactory;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    private final LoanActionService loanActionService;
    private final StrategySuggestionService strategySuggestionService;

    public List<LoanResponseDto> getPendingLoans(Officer officer){

        if(officer.getBranch() == null){
            throw new IllegalStateException("Branch is not set for this officer.");
        }

        List<Loan> pendingLoansForOfficer = loanRepo.findByBranchId(officer.getBranch().getId());

        List<LoanResponseDto> dtos = new ArrayList<>();

        for (Loan loan : pendingLoansForOfficer) {
            LoanResponseDto dto = loanMapper.toDto(loan);
            dto.setLoanStrategy(strategySuggestionService.getSuggestedStrategy(loan));
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

    public LoanResponseDto approveLoan(Officer officer, ApproveRequestDto request){
        Loan loan = loanRepo.findById(request.getLoanId()).orElseThrow(()->new ResourceNotFoundException("Loan"));

        checkIfEligible(loan, officer);

        //When applying loan application this will get set.
//        loan.setInterestRate(GlobalConstant.INTEREST_RATE);

        List<Emi> schedule = strategyFactory.getStrategy(request.getLoanStrategy()).generateSchedule(loan);

        LoanResponseDto dto = loanMapper.toDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));

        loan.setLoanStrategy(request.getLoanStrategy());
        dto.setLoanStrategy(request.getLoanStrategy());

        loanActionService.handleActive(loan);

        return dto;
    }

    public LoanResponseDto rejectLoan(Officer officer, Long loanId){
        Loan loan = loanRepo.findById(loanId).orElseThrow(()->new ResourceNotFoundException("Loan"));

        checkIfEligible(loan, officer);

        loanActionService.handleRejected(loan);

        return loanMapper.toDto(loan);
    }
}
