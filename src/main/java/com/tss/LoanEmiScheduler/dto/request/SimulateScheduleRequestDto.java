package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimulateScheduleRequestDto {
    private String loanNumber;
    private LoanStrategy loanStrategy;
}
