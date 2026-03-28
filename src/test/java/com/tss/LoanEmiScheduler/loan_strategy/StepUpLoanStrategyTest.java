package com.tss.LoanEmiScheduler.loan_strategy;

import com.tss.LoanEmiScheduler.entity.*;
import com.tss.LoanEmiScheduler.enums.EmiStatus;
import com.tss.LoanEmiScheduler.enums.PaymentAllocationType;
import com.tss.LoanEmiScheduler.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepUpLoanStrategyTest {

    @InjectMocks
    private StepUpLoanStrategy strategy;

    @Mock
    private EmiRepository emiRepo;

    @Mock
    private PaymentAllocationRepository paymentRepo;

    @Mock
    private LoanRepository loanRepo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ===============================
    // 1. GENERATE SCHEDULE BASIC TEST
    // ===============================
    @Test
    void shouldGenerateStepUpSchedule() {

        Loan loan = createLoan(100000, 12, 12);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        assertEquals(12, emis.size());

    }

    // ======================================
    // 2. STEP INCREASE AFTER 12 MONTHS
    // ======================================
    @Test
    void shouldIncreaseEmiAfterOneYear() {

        Loan loan = createLoan(200000, 24, 10);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        BigDecimal year1 = emis.get(0).getEmiAmount();
        BigDecimal year2 = emis.get(12).getEmiAmount();

        assertTrue(year2.compareTo(year1) > 0);
    }

    // ======================================
    // 3. ZERO INTEREST CASE
    // ======================================
    @Test
    void shouldHandleZeroInterestLoan() {

        Loan loan = createLoan(120000, 12, 0);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        BigDecimal expected = new BigDecimal("10000");

        assertEquals(0, emis.get(0).getEmiAmount().compareTo(expected));
    }

    // ======================================
    // 4. RE-AMORTIZATION WITH OVERPAYMENT
    // ======================================
    @Test
    void shouldReduceOutstandingOnOverpayment() {

        Loan loan = createLoan(100000, 12, 10);
        loan.setOutstandingBalance(new BigDecimal("80000"));

        Emi trigger = createEmi(1, new BigDecimal("10000"), new BigDecimal("800"), new BigDecimal("9200"));

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(mockFutureEmis(loan, 2, 12));

        when(paymentRepo.findByEmiId(trigger.getId()))
                .thenReturn(List.of(
                        createAllocation("11000")
                ));

        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
                .thenReturn(Collections.emptyList());

        List<Emi> result = strategy.reAmortize(loan, trigger);

        assertNotNull(result);
        verify(loanRepo).save(loan);
    }

    // ======================================
    // 5. OVERDUE CASE (CAPITALIZATION)
    // ======================================
    @Test
    void shouldCapitalizeInterestWhenOverdue() {

        Loan loan = createLoan(100000, 12, 10);
        loan.setOutstandingBalance(new BigDecimal("90000"));

        Emi trigger = createEmi(1, new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("9000"));
        trigger.setEmiStatus(EmiStatus.OVERDUE);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(mockFutureEmis(loan, 2, 12));

        when(paymentRepo.findByEmiIdAndPaymentAllocationType(any(), any()))
                .thenReturn(List.of(createAllocation("500"))); // partial interest

        when(paymentRepo.findByEmiId(any()))
                .thenReturn(Collections.emptyList());

        strategy.reAmortize(loan, trigger);

        assertTrue(loan.getOutstandingBalance().compareTo(new BigDecimal("90000")) > 0);
    }

    // ======================================
    // 6. NO FUTURE EMIS → EXCEPTION
    // ======================================
    @Test
    void shouldThrowIfNoFutureEmis() {

        Loan loan = createLoan(100000, 1, 10);

        Emi trigger = createEmi(1, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE);

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(true);
        when(emiRepo.findByLoanIdAndIsActive(any(), eq(true)))
                .thenReturn(List.of(trigger));

        assertThrows(RuntimeException.class,
                () -> strategy.reAmortize(loan, trigger));
    }

    // ======================================
    // 🔥 7. FULL 120 MONTH SIMULATION
    // ======================================
    @Test
    void shouldSimulateFull120MonthLoanCorrectly() {

        Loan loan = createLoan(1_000_000, 120, 10);
        loan.setOutstandingBalance(new BigDecimal("1000000"));

        when(emiRepo.existsByLoanId(loan.getId())).thenReturn(false);

        List<Emi> emis = strategy.generateSchedule(loan);

        assertEquals(120, emis.size());

        BigDecimal prevBalance = loan.getPrincipalAmount();

        for (int i = 0; i < emis.size(); i++) {

            Emi emi = emis.get(i);

            // Step pattern check
            if (i >= 12) {
                assertTrue(
                        emis.get(i).getEmiAmount()
                                .compareTo(emis.get(i - 12).getEmiAmount()) >= 0
                );
            }

            // Financial correctness
            BigDecimal interest = emi.getInterestComponent();
            BigDecimal principal = emi.getPrincipalComponent();

            assertNotNull(interest);
            assertNotNull(principal);

            prevBalance = prevBalance.subtract(principal);
        }

        // Final balance approx zero
        assertTrue(prevBalance.abs().compareTo(new BigDecimal("5")) < 0);
    }

    // ===============================
    // HELPERS
    // ===============================

    private Loan createLoan(double amount, int tenure, double rate) {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setPrincipalAmount(BigDecimal.valueOf(amount));
        loan.setTenure(tenure);
        loan.setInterestRate(BigDecimal.valueOf(rate));
        loan.setApprovedAt(LocalDateTime.now());
        return loan;
    }

    private Emi createEmi(int no, BigDecimal emi, BigDecimal interest, BigDecimal principal) {
        Emi e = new Emi();
        e.setId((long) no);
        e.setInstallmentNo(no);
        e.setEmiAmount(emi);
        e.setInterestComponent(interest);
        e.setPrincipalComponent(principal);
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

    private PaymentAllocation createAllocation(String amt) {
        PaymentAllocation p = new PaymentAllocation();
        p.setAmountAllocated(new BigDecimal(amt));
        p.setPaymentAllocationType(PaymentAllocationType.INTEREST);
        return p;
    }
}