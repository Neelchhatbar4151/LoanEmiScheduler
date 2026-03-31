package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.entity.Loan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Loan> getLoanHistory(Long loanId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        return reader.createQuery()
                .forRevisionsOfEntity(Loan.class, true, true)
                .add(AuditEntity.id().eq(loanId))
                .getResultList();
    }
}
