package com.lunarhub.repository;

import com.lunarhub.model.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {
    List<JournalEntryLine> findByAccountId(Long accountId);
}
