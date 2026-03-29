package com.tss.LoanEmiScheduler.dto.request;

import com.tss.LoanEmiScheduler.entity.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BranchRequestDto {

    @NotBlank
    private String branchName;

    @NotBlank
    private String branchCode;


    @NotBlank
    private String addressLine1;
    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @Size(min = 6, max = 10)
    @NotBlank
    private String postalCode;
}
