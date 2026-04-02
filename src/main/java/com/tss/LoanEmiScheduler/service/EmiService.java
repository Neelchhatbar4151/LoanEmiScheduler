package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.response.EmiResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.entity.Borrower;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.User;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmiService {
    private final EmiRepository emiRepo;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final EmiMapper emiMapper;

    public EmiResponseDto getEmi(Long emiId){
        Emi emi = emiRepo.findById(emiId).orElseThrow(()->new ResourceNotFoundException("Emi"));
        return emiMapper.toDto(emi);
    }

    public List<EmiResponseDto> getFutureEmiForLoan(String loanNumber){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }
        Loan loan = loanRepository.findByLoanNumber(loanNumber).orElseThrow();
        List<Emi> emiList = null;
        if(loan.getBorrower()
                .getId()
                .equals(user.getId())
        ){
             emiList = emiRepo.findEmiByLoanIdAndDueDateAfterOrderByDueDateAsc(loan.getId(), LocalDate.now());
        }
        if(emiList == null)
            throw new ResourceNotFoundException("Emi");
        return emiMapper.toDtoList(emiList);
    }


    public EmiResponseDto getNextEmiForLoan(String loanNumber){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }
        Loan loan = loanRepository.findByLoanNumber(loanNumber).orElseThrow();
        Emi emi = null;
        if(loan.getBorrower()
                .getId()
                .equals(user.getId())
        ){
            emi = emiRepo.findFirstEmiByLoanIdAndDueDateAfterOrderByDueDateAsc(loan.getId(), LocalDate.now());
        }
        if(emi == null)
            throw new ResourceNotFoundException("Emi");
        return emiMapper.toDto(emi);
    }

    public List<EmiResponseDto> getPastEmiForLoan(String loanNumber){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }
        Loan loan = loanRepository.findByLoanNumber(loanNumber).orElseThrow();
        List<Emi> emiList = null;
        if(loan.getBorrower()
                .getId()
                .equals(user.getId())
        ){
            emiList = emiRepo.findEmiByLoanIdAndDueDateBeforeOrderByDueDate(loan.getId(), LocalDate.now());
        }
        if(emiList == null)
            throw new ResourceNotFoundException("Emi");
        return emiMapper.toDtoList(emiList);
    }
}
