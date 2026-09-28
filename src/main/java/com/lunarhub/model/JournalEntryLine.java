package com.lunarhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "journal_entry_lines")
public class JournalEntryLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntry journalEntry) { this.journalEntry = journalEntry; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }
    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }
}
