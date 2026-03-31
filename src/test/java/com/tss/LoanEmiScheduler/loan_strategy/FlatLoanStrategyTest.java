package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.dto.response.LoanResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.entity.PaymentAllocation;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.exception.AmortizationNotPossibleException;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.exception.ScheduleAlreadyExistsException;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import com.tss.LoanEmiScheduler.repository.PaymentAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlatLoanStrategyTest {

    @Mock private EmiRepository emiRepo;
    @Mock private PaymentAllocationRepository paymentRepo;
    @Mock private LoanRepository loanRepo;
    @Mock private LoanMapper loanMapper;
    @Mock private EmiMapper emiMapper;

    @InjectMocks
    private FlatLoanStrategy strategy;

    private Loan loan;

    @BeforeEach
    void setup() {
        loan = new Loan();
        loan.setId(1L);
        loan.setPrincipalAmount(BigDecimal.valueOf(12000));
        loan.setInterestRate(BigDecimal.valueOf(12));
        loan.setTenure(12);
        loan.setApprovedAt(LocalDateTime.now());
        loan.setOutstandingBalance(BigDecimal.valueOf(12000));
    }

    // ------------------- GENERATE SCHEDULE -------------------

    @Test
    void generateSchedule_success() {
        when(emiRepo.existsByLoanId(1L)).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        assertEquals(12, emis.size());
        assertEquals(EmiStatus.PENDING, emis.get(0).getEmiStatus());
        assertNotNull(emis.get(0).getEmiAmount());
    }

    @Test
    void generateSchedule_shouldThrow_ifAlreadyExists() {
        when(emiRepo.existsByLoanId(1L)).thenReturn(true);

        assertThrows(ScheduleAlreadyExistsException.class,
                () -> strategy.generateSchedule(loan));
    }

    // ------------------- GET EMI SCHEDULE -------------------

    @Test
    void getEmiSchedule_success() {
        List<Emi> emis = List.of(new Emi(), new Emi());
        when(emiRepo.findEmiScheduleAsOfDate(eq(1L), any())).thenReturn(emis);

        LoanResponseDto dto = new LoanResponseDto();
        when(loanMapper.toDto(any())).thenReturn(dto);
        when(emiMapper.toDtoList(emis)).thenReturn(List.of());

        LoanResponseDto result = strategy.getEmiSchedule(loan, LocalDate.now());

        assertNotNull(result);
        verify(emiRepo).findEmiScheduleAsOfDate(eq(1L), any());
    }

    @Test
    void reAmortize_shouldThrow_ifLastEmi() {
        when(emiRepo.existsByLoanId(1L)).thenReturn(true);

        Emi emi = new Emi();
        emi.setInstallmentNo(12);

        when(emiRepo.findByLoanIdAndIsActive(1L, true))
                .thenReturn(List.of(emi));

        assertThrows(AmortizationNotPossibleException.class,
                () -> strategy.reAmortize(emi));
    }

    @Test
    void reAmortize_overpayment_shouldReduceOutstanding() {
        when(emiRepo.existsByLoanId(1L)).thenReturn(true);

        Emi trigger = new Emi();
        trigger.setId(1L);
        trigger.setInstallmentNo(1);
        trigger.setInterestComponent(BigDecimal.valueOf(100));
        trigger.setPrincipalComponent(BigDecimal.valueOf(900));
        trigger.setPenalInterest(BigDecimal.ZERO);
        trigger.setEmiStatus(EmiStatus.PENDING);

        Emi future = new Emi();
        future.setInstallmentNo(2);
        future.setVersion(1);

        when(emiRepo.findByLoanIdAndIsActive(1L, true))
                .thenReturn(List.of(trigger, future));

        PaymentAllocation alloc = new PaymentAllocation();
        alloc.setAmountAllocated(BigDecimal.valueOf(2000)); // overpayment

        when(paymentRepo.findByEmiId(1L)).thenReturn(List.of(alloc));

//        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
//                .thenReturn(Collections.emptyList());

        List<Emi> result = strategy.reAmortize(trigger);

        assertFalse(result.isEmpty());
        assertTrue(loan.getOutstandingBalance().compareTo(BigDecimal.valueOf(12000)) < 0);
    }

    @Test
    void reAmortize_overdue_shouldCapitalizeInterest() {
        when(emiRepo.existsByLoanId(1L)).thenReturn(true);

        Emi trigger = new Emi();
        trigger.setId(1L);
        trigger.setInstallmentNo(1);
        trigger.setInterestComponent(BigDecimal.valueOf(500));
        trigger.setPrincipalComponent(BigDecimal.valueOf(500));
        trigger.setPenalInterest(BigDecimal.ZERO);
        trigger.setEmiStatus(EmiStatus.OVERDUE);

        Emi future = new Emi();
        future.setInstallmentNo(2);
        future.setVersion(1);

        when(emiRepo.findByLoanIdAndIsActive(1L, true))
                .thenReturn(List.of(trigger, future));

        when(paymentRepo.findByEmiId(1L))
                .thenReturn(Collections.emptyList());

        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
                .thenReturn(Collections.emptyList());

        BigDecimal before = loan.getOutstandingBalance();

        strategy.reAmortize(trigger);

        assertTrue(loan.getOutstandingBalance().compareTo(before) > 0);
    }

    @Test
    void reAmortize_shouldDeactivateOldEmis() {
        when(emiRepo.existsByLoanId(1L)).thenReturn(true);

        Emi trigger = new Emi();
        trigger.setId(1L);
        trigger.setInstallmentNo(1);
        trigger.setInterestComponent(BigDecimal.valueOf(100));
        trigger.setPrincipalComponent(BigDecimal.valueOf(900));
        trigger.setPenalInterest(BigDecimal.ZERO);
        trigger.setEmiStatus(EmiStatus.PENDING);

        Emi future = new Emi();
        future.setInstallmentNo(2);
        future.setVersion(1);
        future.setIsActive(true);

        when(emiRepo.findByLoanIdAndIsActive(1L, true))
                .thenReturn(List.of(trigger, future));

        when(paymentRepo.findByEmiId(any()))
                .thenReturn(Collections.emptyList());

//        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
//                .thenReturn(Collections.emptyList());

        strategy.reAmortize(trigger);

        assertFalse(future.getIsActive());
//        verify(emiRepo).saveAll(any());
    }
}