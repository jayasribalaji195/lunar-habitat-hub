package com.lunarhub.repository;

import com.lunarhub.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
