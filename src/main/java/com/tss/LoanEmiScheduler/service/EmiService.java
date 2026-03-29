package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.response.EmiResponseDto;
import com.tss.LoanEmiScheduler.dto_mapper.EmiMapper;
import com.tss.LoanEmiScheduler.entity.Emi;
import com.tss.LoanEmiScheduler.exception.ResourceNotFoundException;
import com.tss.LoanEmiScheduler.repository.EmiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmiService {

    private final EmiRepository emiRepo;

    private final EmiMapper emiMapper;

    public EmiResponseDto getEmi(Long emiId){
        Emi emi = emiRepo.findById(emiId).orElseThrow(()->new ResourceNotFoundException("Emi"));
        return emiMapper.toDto(emi);
    }

}
