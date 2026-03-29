package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.request.SimulateScheduleRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanStrategyFactory factory;
    private final LoanRepository loanRepo;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    public LoanResponseDto simulateSchedule(SimulateScheduleRequestDto request){
        Loan loan = loanRepo.findById(request.getLoanId()).orElseThrow(()-> new ResourceNotFoundException("Loan"));
        if(request.getLoanStrategy() == LoanStrategy.REJECT){
            throw new UnsupportedOperationException("Can't make Schedule for Reject Loan Strategy.");
        }
        List<Emi> schedule = factory.getStrategy(request.getLoanStrategy()).generateSchedule(loan);
        LoanResponseDto dto = loanMapper.toDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));

        return dto;
    }

    public LoanResponseDto getLoan(Long loanId){
        Loan loan = loanRepo.findById(loanId).orElseThrow(()-> new ResourceNotFoundException("Loan"));
        return loanMapper.toDto(loan);
    }
}
