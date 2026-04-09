package com.tss.LoanEmiScheduler.enums;

import lombok.Getter;

@Getter
public enum LogTag {

    AUTH("[AUTH]"),
    BRANCH("[BRANCH]"),
    LOAN("[LOAN]"),
    TRANSACTION("[TRANSACTION]"),
    SYSTEM("[SYSTEM]"),
    SECURITY("[SECURITY]"),
    EMAIL("[EMAIL]"),
    EMI("[EMI]");

    private final String value;

    LogTag(String value) {
        this.value = value;
    }
}