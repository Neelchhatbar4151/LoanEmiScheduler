package com.tss.LoanEmiScheduler.constant;

import java.math.BigDecimal;

public class GlobalConstant {
    public static final BigDecimal INTEREST_RATE = new BigDecimal("15");
    public static final BigDecimal PENAL_INTEREST_RATE = new BigDecimal("20");
    public static final String ACCOUNT_NUMBER_COUNTER_KEY = "account_number_counter";
    public static final String LOAN_NUMBER_COUNTER_KEY = "loan_number_counter";
    public static final BigDecimal PENALTY_AMOUNT = new BigDecimal("500");

//    LOGGING CONSTANTS
    public static final String AUTH = "[AUTH]";
    public static final String BRANCH = "[BRANCH]";
    public static final String LOAN = "[LOAN]";
    public static final String TRANSACTION = "[TRANSACTION]";
    public static final String SYSTEM = "[SYSTEM]";
    public static final String SECURITY = "[SECURITY]";
    public static final String EMAIL = "[EMAIL]";
    public static final String EMI = "[EMI]";
}
