package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.exception.*;
import com.tss.LoanEmiScheduler.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReducingBalanceLoanStrategyTest {

    @InjectMocks
    private ReducingBalanceLoanStrategy strategy;

    @Mock
    private EmiRepository emiRepo;

    @Mock
    private PaymentAllocationRepository paymentRepo;

    @Mock
    private LoanRepository loanRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ======================================
    // 1. GENERATE SCHEDULE - BASIC
    // ======================================
    @Test
    void shouldGenerateScheduleSuccessfully() {

        Loan loan = createLoan(100000, 12, 12);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        assertEquals(12, emis.size());

        // EMI should be constant (except last)
        BigDecimal firstEmi = emis.get(0).getEmiAmount();
        for (int i = 0; i < 11; i++) {
            assertEquals(0, emis.get(i).getEmiAmount().compareTo(firstEmi));
        }
    }

    // ======================================
    // 2. INTEREST DECREASING, PRINCIPAL INCREASING
    // ======================================
    @Test
    void shouldHaveReducingInterestAndIncreasingPrincipal() {

        Loan loan = createLoan(100000, 12, 12);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        for (int i = 1; i < emis.size(); i++) {
            assertTrue(
                    emis.get(i).getInterestComponent()
                            .compareTo(emis.get(i - 1).getInterestComponent()) <= 0
            );

            assertTrue(
                    emis.get(i).getPrincipalComponent()
                            .compareTo(emis.get(i - 1).getPrincipalComponent()) >= 0
            );
        }
    }

    // ======================================
    // 3. ZERO INTEREST CASE
    // ======================================
    @Test
    void shouldHandleZeroInterest() {

        Loan loan = createLoan(120000, 12, 0);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        BigDecimal expected = new BigDecimal("10000");

        for (Emi emi : emis) {
            assertEquals(0, emi.getEmiAmount().compareTo(expected));
            assertEquals(0, emi.getInterestComponent().intValue());
        }
    }

    // ======================================
    // 4. THROW IF SCHEDULE EXISTS
    // ======================================
    @Test
    void shouldThrowIfScheduleAlreadyExists() {

        Loan loan = createLoan(100000, 12, 10);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);

        assertThrows(ScheduleAlreadyExistsException.class,
                () -> strategy.generateSchedule(loan));
    }

    // ======================================
    // 5. RE-AMORTIZE NORMAL FLOW
    // ======================================
    @Test
    void shouldReAmortizeSuccessfully() {

        Loan loan = createLoan(100000, 12, 10);
        loan.setOutstandingBalance(new BigDecimal("80000"));

        Emi trigger = createEmi(1, "10000", "800", "9200");

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(mockFutureEmis(loan, 2, 12));

        when(paymentRepo.findByEmiId(trigger.getId()))
                .thenReturn(Collections.emptyList());

        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
                .thenReturn(Collections.emptyList());

        List<Emi> result = strategy.reAmortize(trigger);

        assertNotNull(result);
        assertEquals(11, result.size());

        verify(loanRepo).save(loan);
    }

    // ======================================
    // 6. OVERPAYMENT CASE
    // ======================================
    @Test
    void shouldReduceOutstandingOnOverpayment() {

        Loan loan = createLoan(100000, 12, 10);
        loan.setOutstandingBalance(new BigDecimal("90000"));

        Emi trigger = createEmi(1, "10000", "800", "9200");

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(mockFutureEmis(loan, 2, 12));

        when(paymentRepo.findByEmiId(trigger.getId()))
                .thenReturn(List.of(createAllocation("12000"))); // overpay

        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
                .thenReturn(Collections.emptyList());

        strategy.reAmortize(trigger);

        assertTrue(
                loan.getOutstandingBalance().compareTo(new BigDecimal("90000")) < 0
        );
    }

    // ======================================
    // 7. OVERDUE (CAPITALIZATION)
    // ======================================
    @Test
    void shouldCapitalizeInterestWhenOverdue() {

        Loan loan = createLoan(100000, 12, 10);
        loan.setOutstandingBalance(new BigDecimal("90000"));

        Emi trigger = createEmi(1, "10000", "1000", "9000");
        trigger.setEmiStatus(EmiStatus.OVERDUE);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(mockFutureEmis(loan, 2, 12));

        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
                .thenReturn(List.of(createAllocation("500"))); // partial interest

        when(paymentRepo.findByEmiId(any()))
                .thenReturn(Collections.emptyList());

        strategy.reAmortize(trigger);

        assertTrue(
                loan.getOutstandingBalance().compareTo(new BigDecimal("90000")) > 0
        );
    }

    // ======================================
    // 8. NO FUTURE EMIS
    // ======================================
    @Test
    void shouldThrowIfNoFutureEmis() {

        Loan loan = createLoan(100000, 1, 10);

        Emi trigger = createEmi(1, "10000", "800", "9200");

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(List.of(trigger));

        assertThrows(AmortizationNotPossibleException.class,
                () -> strategy.reAmortize(trigger));
    }

    // ======================================
    // 9. FINANCIAL CORRECTNESS (FULL PAYDOWN)
    // ======================================
    @Test
    void shouldFullyPayLoan() {

        Loan loan = createLoan(100000, 12, 10);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        BigDecimal balance = loan.getPrincipalAmount();

        for (Emi emi : emis) {
            balance = balance.subtract(emi.getPrincipalComponent());
        }

        assertTrue(balance.abs().compareTo(new BigDecimal("2")) < 0);
    }

    // ======================================
    // HELPERS
    // ======================================

    private Loan createLoan(double amount, int tenure, double rate) {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setPrincipalAmount(BigDecimal.valueOf(amount));
        loan.setTenure(tenure);
        loan.setInterestRate(BigDecimal.valueOf(rate));
        loan.setApprovedAt(LocalDateTime.now());
        return loan;
    }

    private Emi createEmi(int no, String emi, String interest, String principal) {
        Emi e = new Emi();
        e.setId((long) no);
        e.setInstallmentNo(no);
        e.setEmiAmount(new BigDecimal(emi));
        e.setInterestComponent(new BigDecimal(interest));
        e.setPrincipalComponent(new BigDecimal(principal));
        e.setEmiStatus(EmiStatus.PENDING);
        e.setIsActive(true);
        e.setVersion(1);
        return e;
    }

    private List<Emi> mockFutureEmis(Loan loan, int start, int end) {
        List<Emi> list = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            Emi e = new Emi();
            e.setLoan(loan);
            e.setInstallmentNo(i);
            e.setEmiAmount(new BigDecimal("10000"));
            e.setIsActive(true);
            e.setVersion(1);
            list.add(e);
        }
        return list;
    }

    private PaymentAllocation createAllocation(String amount) {
        PaymentAllocation p = new PaymentAllocation();
        p.setAmountAllocated(new BigDecimal(amount));
        p.setPaymentAllocationType(PaymentAllocationType.INTEREST);
        return p;
    }
}