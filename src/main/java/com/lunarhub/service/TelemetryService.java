package com.lunarhub.service;

import com.lunarhub.dto.TelemetryRequest;
import com.lunarhub.model.HabitatModule;
import com.lunarhub.model.TelemetryReading;
import com.lunarhub.repository.HabitatModuleRepository;
import com.lunarhub.repository.TelemetryReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ingests environmental telemetry, auto-adjusts scrubbers when a reading falls
 * outside a module's nominal thresholds, and accumulates gas/water consumption
 * that later drives tenant utility billing.
 */
@Service
public class TelemetryService {

    private final HabitatModuleRepository moduleRepository;
    private final TelemetryReadingRepository telemetryRepository;

    public TelemetryService(HabitatModuleRepository moduleRepository,
                             TelemetryReadingRepository telemetryRepository) {
        this.moduleRepository = moduleRepository;
        this.telemetryRepository = telemetryRepository;
    }

    /** POST /api/lunar/telemetry — records a reading and auto-adjusts scrubbers if thresholds are breached. */
    @Transactional
    public TelemetryReading ingest(TelemetryRequest request) {
        HabitatModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Habitat module not found: " + request.getModuleId()));

        TelemetryReading reading = new TelemetryReading();
        reading.setModule(module);
        reading.setTimestamp(LocalDateTime.now());
        reading.setAtmosphericPressureKPa(request.getAtmosphericPressureKPa());
        reading.setWaterPurityPct(request.getWaterPurityPct());
        reading.setOxygenLevelPct(request.getOxygenLevelPct());
        reading.setCo2LevelPpm(request.getCo2LevelPpm());
        reading.setOxygenConsumedM3(nvl(request.getOxygenConsumedM3()));
        reading.setWaterConsumedLiters(nvl(request.getWaterConsumedLiters()));

        boolean pressureOut = request.getAtmosphericPressureKPa() != null
                && (request.getAtmosphericPressureKPa().compareTo(module.getPressureMinKPa()) < 0
                    || request.getAtmosphericPressureKPa().compareTo(module.getPressureMaxKPa()) > 0);
        boolean purityLow = request.getWaterPurityPct() != null
                && request.getWaterPurityPct().compareTo(module.getWaterPurityMinPct()) < 0;

        boolean breach = pressureOut || purityLow;
        reading.setScrubberAdjusted(breach);
        module.setStatus(breach ? HabitatModule.ModuleStatus.ALERT : HabitatModule.ModuleStatus.NOMINAL);

        moduleRepository.save(module);
        return telemetryRepository.save(reading);
    }

    public List<TelemetryReading> history(Long moduleId) {
        return telemetryRepository.findByModuleIdOrderByTimestampDesc(moduleId);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
