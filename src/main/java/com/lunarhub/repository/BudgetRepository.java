package com.lunarhub.repository;

import com.lunarhub.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByAnalyticAccountId(Long analyticAccountId);
}
