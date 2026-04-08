package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.constant.GlobalConstant;
import com.tss.LoanEmiScheduler.dto.request.LoanApplyRequestDto;
import com.tss.LoanEmiScheduler.dto.request.SimulateScheduleRequestDto;
import com.tss.LoanEmiScheduler.dto.response.BorrowerLoanResponseDto;
import com.tss.LoanEmiScheduler.dto.response.EmiScheduleResponseDto;
import com.tss.LoanEmiScheduler.dto.response.LoanApplyResponseDto;
import com.tss.LoanEmiScheduler.dto.response.OfficerLoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.*;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.factory.LoanStrategyFactory;
import com.tss.LoanEmiScheduler.repository.GlobalConfigRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanStrategyFactory factory;
    private final LoanRepository loanRepo;
    private final UserRepository userRepository;
    private final GlobalConfigRepository globalConfigRepository;

    private final LoanMapper loanMapper;
    private final EmiMapper emiMapper;

    private final NotificationService notificationService;
    private final StrategySuggestionService strategySuggestionService;

    private static Long loanNumberCounter;

    public EmiScheduleResponseDto simulateSchedule(SimulateScheduleRequestDto request){
        Loan loan = loanRepo.findByLoanNumber(request.getLoanNumber()).orElseThrow(()-> new ResourceNotFoundException("Loan"));

        if(loan.getLoanStatus() != LoanStatus.APPLIED){
            throw new UnsupportedOperationException("This Feature is only supported for Loan Applications");
        }

        List<Emi> schedule = factory.getStrategy(request.getLoanStrategy()).generateSchedule(loan);
        EmiScheduleResponseDto dto = loanMapper.toEmiScheduleResponseDto(loan);
        dto.setEmis(emiMapper.toDtoList(schedule));

        return dto;
    }

    public LoanApplyResponseDto applyLoan(LoanApplyRequestDto loanApplyRequestDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.BORROWER)){
            throw new BadCredentialsException("Not a borrower.");
        }
        log.info("{} Apply: Loan {} by borrower id: {}", LogTag.LOAN.getValue(), loanApplyRequestDto, user.getId());

        if(loanRepo.countByBorrower(((Borrower) user).getAccountNumber()) >= 3){
            throw new IllegalStateException("Can't have more than 3 Ongoing loans.");
        }

        Loan loan = loanMapper.toLoan(loanApplyRequestDto);
        loan.setLoanNumber(generateLoanNumber());
        loan.setBorrower((Borrower) user);
        loan.setInterestRate(GlobalConstant.INTEREST_RATE);
        loan.setBranch(((Borrower) user).getBranch());
        loan.setLoanStatus(LoanStatus.APPLIED);
        loan.setOutstandingBalance(loanApplyRequestDto.getPrincipalAmount());
        loan = loanRepo.save(loan);
        log.info("{} Apply: Saved loan id {} and loan number {} by borrower id: {}", LogTag.LOAN.getValue(), loan.getId(), loan.getLoanNumber(), user.getId());

        try{
            Map<String, Object> variables = new HashMap<>();
            variables.put("amount", loan.getPrincipalAmount());
            variables.put("name", loan.getBorrower().getFirstName());

            notificationService.sendNotification(loan.getBorrower().getEmail(), NotificationType.APPLICATION, variables);
            log.info("{} Email: Sent to {} for loan number {}", LogTag.LOAN.getValue(), borrowerIdentifier, loan.getLoanNumber());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return loanMapper.toLoanApplyResponseDto(loan);
    }

    public Page<BorrowerLoanResponseDto> findLoanByBorrower(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if (!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }

        String accountNumber = ((Borrower) user).getAccountNumber();
        Page<Loan> loanList = loanRepo.findByBorrowerAccountNumber(accountNumber, pageable);
        if (loanList.isEmpty())
            throw new ResourceNotFoundException("Loans");
        return loanList.map(loanMapper::toBorrowerLoanResponseDto);
    }

    public BorrowerLoanResponseDto findLoanByLoanNumber(String loanNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (!(user instanceof Borrower)) {
            throw new SecurityException("Access denied: Not a borrower.");
        }

        String accountNumber = ((Borrower) user).getAccountNumber();
        Loan loan = loanRepo.findByLoanNumberAndBorrowerAccountNumber(loanNumber, accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan"));
        return loanMapper.toBorrowerLoanResponseDto(loan);
    }

    public Page<BorrowerLoanResponseDto> findLoanByBorrowerWithStatus(LoanStatus loanStatus, Pageable pageable){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(borrowerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.BORROWER)) {
            throw new SecurityException("Not a borrower.");
        }

        String accountNumber = ((Borrower) user).getAccountNumber();
        Page<Loan> loanList = loanRepo.findByLoanStatusAndBorrowerAccountNumber(loanStatus, accountNumber, pageable);
        if(loanList.isEmpty())
            throw new ResourceNotFoundException("Loans");
        return loanList.map(loanMapper::toBorrowerLoanResponseDto);
    }

    public Page<OfficerLoanResponseDto> findLoanByBranchId(Pageable pageable){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String officerIdentifier = authentication.getName();
        User user = userRepository.findByIdentifier(officerIdentifier).orElseThrow();

        if(!user.getRole().equals(Role.OFFICER)) {
            throw new SecurityException("Not a officer.");
        }

        Long branchId = ((Officer) user).getBranch().getId();
        Page<Loan> loanList = loanRepo.findByBranchId(branchId, pageable);
        if(loanList.isEmpty())
            throw new ResourceNotFoundException("Loans");
//        return loanList.map(loanMapper::toOfficerLoanResponseDto);

        return loanList.map(loan -> {
            OfficerLoanResponseDto dto = loanMapper.toOfficerLoanResponseDto(loan);
            if (loan.getLoanStatus().equals(LoanStatus.APPLIED)) {
                dto.setSuggestedStrategy(strategySuggestionService.getSuggestedStrategy(loan));
            }
            return dto;
        });
    }

    private String generateLoanNumber(){
        return "LN"+loanNumberCounter++;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initLoanNumberCounter(){
        loanNumberCounter = Long.valueOf(
                globalConfigRepository
                        .findByKey(GlobalConstant.LOAN_NUMBER_COUNTER_KEY)
                        .orElseThrow()
                        .getValue()
        );
    }

    @EventListener(ContextClosedEvent.class)
    public void runBeforeShutdown() {
        globalConfigRepository.save(
                new GlobalConfig(
                        GlobalConstant.LOAN_NUMBER_COUNTER_KEY,
                        loanNumberCounter.toString()
                )
        );
    }

}
