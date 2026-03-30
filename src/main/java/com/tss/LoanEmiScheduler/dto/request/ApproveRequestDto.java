package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import lombok.Data;

@Data
public class ApproveRequestDto {
    private Long loanId;  //use loan number
    private LoanStrategy loanStrategy;
}
