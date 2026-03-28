package com.tss.LoanEmiScheduler.factory;

import com.tss.LoanEmiScheduler.enums.LoanStrategy;
import com.tss.LoanEmiScheduler.loan_strategy.ILoanStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoanStrategyFactory {
    private final Map<LoanStrategy, ILoanStrategy> strategyMap;

    public LoanStrategyFactory(List<ILoanStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(ILoanStrategy::getType, s -> s));
    }

    public ILoanStrategy getStrategy(LoanStrategy type) {
        return strategyMap.get(type);
    }
}
