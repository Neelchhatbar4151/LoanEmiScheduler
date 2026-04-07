package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.action_service.LoanActionService;
import com.tss.LoanEmiScheduler.dto.request.ApproveRequestDto;
import com.tss.LoanEmiScheduler.dto.request.RejectRequestDto;
import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.LoanStatus;
import com.tss.LoanEmiScheduler.enums.NotificationType;
import com.tss.LoanEmiScheduler.enums.Role;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.BorrowerRepository;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.LOAN;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficerService {
    private final LoanRepository loanRepo;
    private final UserRepository userRepository;
    private final EmiRepository emiRepository;
    private final BorrowerRepository borrowerRepository;

    private final LoanStrategyFactory strategyFactory;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    private final NotificationService notificationService;

    private final LoanActionService loanActionService;
    private final StrategySuggestionService strategySuggestionService;

    public List<LoanResponseDto> getAllLoans(LoanStatus loanStatus){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String officerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(officerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not an officer.");
        }

        Officer officer = ((Officer) user);

        List<Loan> pendingLoansForOfficer = loanRepo.findByBranchIdAndLoanStatus(officer.getBranch().getId(), loanStatus);
        List<LoanResponseDto> dtos = new ArrayList<>();
        for (Loan loan : pendingLoansForOfficer) {
            LoanResponseDto dto = loanMapper.toDto(loan);
            if(loan.getLoanStatus().equals(LoanStatus.APPLIED))
                dto.setSuggestedStrategy(strategySuggestionService.getSuggestedStrategy(loan));
            dtos.add(dto);
        }
        return dtos;
    }

    public List<LoanResponseDto> getAllLoansByOfficer(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String officerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(officerIdentifier).orElseThrow();
        if(!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not an officer.");
        }

        Officer officer = ((Officer) user);

        List<Loan> allLoans = loanRepo.findByOfficerId(officer.getId());
        List<LoanResponseDto> dtos = new ArrayList<>();
        for (Loan loan : allLoans) {
            LoanResponseDto dto = loanMapper.toDto(loan);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<LoanResponseDto> findLoanByBorrower(String  accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if (!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not a officer.");
        }

        Long branchId = ((Officer) user).getBranch().getId();
        Long borrowerId = borrowerRepository.findByAccountNumber(accountNumber).orElseThrow().getId();
        List<Loan> loanList = loanRepo.findByBranchIdAndBorrowerId(branchId, borrowerId);
        List<LoanResponseDto> dtos = new ArrayList<>();
        for (Loan loan : loanList) {
            LoanResponseDto dto = loanMapper.toDto(loan);
            if(loan.getLoanStatus().equals(LoanStatus.APPLIED))
                dto.setSuggestedStrategy(strategySuggestionService.getSuggestedStrategy(loan));
            dtos.add(dto);
        }
        if (loanList.isEmpty())
            throw new ResourceNotFoundException("Loans");
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

        log.info("{} Approve: Approving loan number {} applied by borrower number {} for branch code {} approve by officer username {}",
                LOAN,
                loan.getLoanNumber(),
                loan.getBorrower().getAccountNumber(),
                loan.getBranch().getBranchCode(),
                officer.getUsername()
        );

        //When applying loan application this will get set.
//        loan.setInterestRate(GlobalConstant.INTEREST_RATE);

        loan.setApprovedAt(LocalDateTime.now());
        loan.setOfficer(officer);
        List<Emi> schedule = strategyFactory.getStrategy(request.getLoanStrategy()).generateSchedule(loan);

        emiRepository.saveAll(schedule);

        log.info("{}[EMI] Emi schedule: created for loan id {} schedule: {}", LOAN, loan.getId(), schedule);

        LoanResponseDto dto = loanMapper.toDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));
        loan.setLoanStrategy(request.getLoanStrategy());
        dto.setLoanStrategy(request.getLoanStrategy());

        loanActionService.handleActive(loan);
        log.info("{} Approve: Loan {} approved with with strategy {}", LOAN, loan.getId(), loan.getLoanStrategy());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("amount", loan.getPrincipalAmount());
            variables.put("loanNumber", loan.getLoanNumber());
            variables.put("name", loan.getBorrower().getFirstName());

            notificationService.sendNotification(loan.getBorrower().getEmail(), NotificationType.APPROVAL, variables);
            log.info("{} Email: Email sent to borrower {} for {} of loan {}", LOAN, user.getId(), NotificationType.APPROVAL, loan.getId());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
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

        log.info("{} Reject: Rejecting loan number {} applied by borrower number {} for branch code {} reject by officer username {}",
                LOAN,
                loan.getLoanNumber(),
                loan.getBorrower().getAccountNumber(),
                loan.getBranch().getBranchCode(),
                officer.getUsername()
        );
        loanActionService.handleRejected(loan);


        Map<String, Object> variables = new HashMap<>();
        variables.put("loanNumber", loan.getLoanNumber());
        variables.put("name", loan.getBorrower().getFirstName());

        try {
            notificationService.sendNotification(loan.getBorrower().getEmail(), NotificationType.REJECTION, variables);
            log.info("{} Email: Email sent to borrower {} for {} of loan {}", LOAN, user.getId(), NotificationType.REJECTION, loan.getId());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        return loanMapper.toDto(loan);
    }
}
