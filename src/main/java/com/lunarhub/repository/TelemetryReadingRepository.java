package com.lunarhub.repository;

import com.lunarhub.model.TelemetryReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

    List<TelemetryReading> findByModuleIdOrderByTimestampDesc(Long moduleId);

    Optional<TelemetryReading> findFirstByModuleIdOrderByTimestampDesc(Long moduleId);

    List<TelemetryReading> findByModuleIdAndTimestampBetween(Long moduleId, LocalDateTime start, LocalDateTime end);
}
