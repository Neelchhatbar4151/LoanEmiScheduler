package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import lombok.Data;

@Data
public class SimulateScheduleRequestDto {
    private Long loanId;
    private LoanStrategy loanStrategy;
}
