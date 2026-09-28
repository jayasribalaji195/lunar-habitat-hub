package com.lunarhub.repository;

import com.lunarhub.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, Long> {
}
