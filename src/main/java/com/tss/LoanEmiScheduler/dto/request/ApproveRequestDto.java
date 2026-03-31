package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ApproveRequestDto {
    @NotBlank
    private String loanNumber;  //use loan number
    @NotNull
    private LoanStrategy loanStrategy;
}
