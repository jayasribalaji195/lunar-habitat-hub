package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analytic_account_id")
    private AnalyticAccount analyticAccount;

    private BigDecimal plannedAmount;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnalyticAccount getAnalyticAccount() { return analyticAccount; }
    public void setAnalyticAccount(AnalyticAccount analyticAccount) { this.analyticAccount = analyticAccount; }
    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
}
