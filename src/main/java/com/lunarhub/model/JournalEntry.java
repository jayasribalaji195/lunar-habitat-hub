package com.lunarhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_id")
    private Journal journal;

    private LocalDateTime date;
    private String description;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntryLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Journal getJournal() { return journal; }
    public void setJournal(Journal journal) { this.journal = journal; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<JournalEntryLine> getLines() { return lines; }
    public void setLines(List<JournalEntryLine> lines) { this.lines = lines; }
}
