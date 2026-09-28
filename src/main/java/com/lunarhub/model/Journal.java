package com.lunarhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "journals")
public class Journal {
    public enum JournalType { SALES, PURCHASE, CASH, BANK }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private JournalType type;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public JournalType getType() { return type; }
    public void setType(JournalType type) { this.type = type; }
}
