package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.response.LoanAuditDto;
import com.tss.LoanEmiScheduler.dto_mapper.LoanMapper;
import com.tss.LoanEmiScheduler.entity.Loan;
import com.tss.LoanEmiScheduler.repository.LoanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public List<LoanAuditDto> getLoanHistory(String loanNumber) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        return loanMapper.toAuditDtoList(reader.createQuery()
                .forRevisionsOfEntity(Loan.class, true, true)
                .add(AuditEntity.id().eq(loanRepository.findByLoanNumber(loanNumber).orElseThrow().getId()))
                .getResultList());
    }
}
