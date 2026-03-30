package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.dto.request.LoanApplyRequestDto;
import com.tss.LoanEmiScheduler.dto.request.SimulateScheduleRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanApplyResponseDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Borrower;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.User;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanStrategyFactory factory;
    private final LoanRepository loanRepo;
    private final UserRepository userRepository;

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

    public LoanApplyResponseDto applyLoan(LoanApplyRequestDto loanApplyRequestDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.BORROWER)){
            throw new BadCredentialsException("Not a borrower.");
        }

        Loan loan = loanMapper.toLoan(loanApplyRequestDto);
        loan.setLoanNumber(generateLoanNumber());
        loan.setBorrower((Borrower) user);
        loan.setInterestRate(GlobalConstant.INTEREST_RATE);
        loan.setBranch(((Borrower) user).getBranch());
        loan.setLoanStatus(LoanStatus.APPLIED);
        loan.setOutstandingBalance(loanApplyRequestDto.getPrincipalAmount());
        loan = loanRepo.save(loan);
        return loanMapper.toLoanApplyResponseDto(loan);
    }

    public List<LoanResponseDto> findLoanByBorrower(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }

        String accountNumber = ((Borrower) user).getAccountNumber();
        List<Loan> loanList = loanRepo.findByBorrowerAccountNumber(accountNumber);
        if(loanList.isEmpty())
            throw new ResourceNotFoundException("loans");
        return loanMapper.toDtoList(loanList);
    }

    public LoanResponseDto findLoanByLoanNumber(String loanNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (!(user instanceof Borrower)) {
            throw new SecurityException("Access denied: Not a borrower.");
        }

        String accountNumber = ((Borrower) user).getAccountNumber();
        Loan loan = loanRepo.findByLoanNumberAndBorrowerAccountNumber(loanNumber, accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found or access denied"));

        return loanMapper.toDto(loan);
    }

    private String generateLoanNumber(){
        return "";
    }

}
